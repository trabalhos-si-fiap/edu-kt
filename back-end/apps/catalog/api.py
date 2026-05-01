from django.db.models import Avg, Count, Q
from django.shortcuts import get_object_or_404
from ninja import Query, Router

from apps.accounts.auth import bearer_auth
from apps.catalog.models import Product, Review
from apps.catalog.schemas import (
    CategoryListOut,
    ProductListOut,
    ProductOut,
    ReviewIn,
    ReviewListOut,
    ReviewOut,
)

router = Router()

MAX_LIMIT = 100
DEFAULT_LIMIT = 20


def _annotate_ratings(qs):
    return qs.annotate(
        rating_avg=Avg("reviews__rating"),
        rating_count=Count("reviews"),
    )


def _serialize_product(p: Product) -> dict:
    return {
        "id": p.id,
        "name": p.name,
        "type": p.type,
        "subtype": p.subtype,
        "description": p.description,
        "price": p.price,
        "image_url": p.image_url,
        "rating_avg": round(float(getattr(p, "rating_avg", None) or 0.0), 2),
        "rating_count": int(getattr(p, "rating_count", 0) or 0),
    }


@router.get("", response=ProductListOut)
def list_products(
    request,
    q: str | None = None,
    limit: int = Query(DEFAULT_LIMIT, ge=1, le=MAX_LIMIT),
    offset: int = Query(0, ge=0),
):
    qs = Product.objects.all()
    if q:
        term = q.strip()
        if term:
            qs = qs.filter(
                Q(name__icontains=term)
                | Q(description__icontains=term)
                | Q(type__icontains=term)
                | Q(subtype__icontains=term)
            )
    total = qs.count()
    items = list(_annotate_ratings(qs)[offset : offset + limit])
    return {
        "items": [_serialize_product(p) for p in items],
        "total": total,
        "limit": limit,
        "offset": offset,
    }


@router.get("/categories", response=CategoryListOut)
def list_categories(request):
    rows = (
        Product.objects.exclude(type="")
        .values("type")
        .annotate(count=Count("id"))
        .order_by("type")
    )
    return {"items": [{"type": r["type"], "count": r["count"]} for r in rows]}


@router.get("/{product_id}", response=ProductOut)
def get_product(request, product_id: int):
    product = get_object_or_404(_annotate_ratings(Product.objects.all()), pk=product_id)
    return _serialize_product(product)


@router.get("/{product_id}/reviews", response=ReviewListOut)
def list_product_reviews(
    request,
    product_id: int,
    limit: int = Query(DEFAULT_LIMIT, ge=1, le=MAX_LIMIT),
    offset: int = Query(0, ge=0),
):
    product = get_object_or_404(Product, pk=product_id)
    reviews_qs = product.reviews.all()
    total = reviews_qs.count()
    aggregate = reviews_qs.aggregate(avg=Avg("rating"), count=Count("id"))
    items = list(reviews_qs[offset : offset + limit])
    return {
        "items": items,
        "total": total,
        "rating_avg": round(float(aggregate["avg"] or 0.0), 2),
        "rating_count": int(aggregate["count"] or 0),
    }


@router.post(
    "/{product_id}/reviews",
    auth=bearer_auth,
    response={201: ReviewOut, 400: dict, 404: dict},
)
def create_product_review(request, product_id: int, payload: ReviewIn):
    if payload.rating < 1 or payload.rating > 5:
        return 400, {"detail": "rating must be between 1 and 5"}
    product = get_object_or_404(Product, pk=product_id)
    user = request.auth
    author = (user.get_full_name() or user.email or "").strip()[:120] or "Anônimo"
    review = Review.objects.create(
        product=product,
        author=author,
        rating=payload.rating,
        comment=(payload.comment or "")[:1000],
    )
    return 201, review
