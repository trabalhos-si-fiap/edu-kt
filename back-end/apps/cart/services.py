from decimal import Decimal

from django.db import transaction
from django.db.models import Avg, Count, F

from apps.cart.models import Cart, CartItem
from apps.catalog.models import Product


def get_or_create_cart(user) -> Cart:
    cart, _ = Cart.objects.get_or_create(user=user)
    return cart


@transaction.atomic
def add_item(user, product_id: int, quantity: int = 1) -> Cart:
    if quantity < 1:
        raise ValueError("quantity must be >= 1")
    try:
        product = Product.objects.get(pk=product_id)
    except Product.DoesNotExist as e:
        raise LookupError("Product not found") from e
    cart = get_or_create_cart(user)
    item, created = CartItem.objects.get_or_create(
        cart=cart, product=product, defaults={"quantity": quantity}
    )
    if not created:
        CartItem.objects.filter(pk=item.pk).update(quantity=F("quantity") + quantity)
    return cart


@transaction.atomic
def remove_item(user, product_id: int, quantity: int | None = None) -> Cart:
    cart = get_or_create_cart(user)
    if quantity is None:
        CartItem.objects.filter(cart=cart, product_id=product_id).delete()
        return cart
    if quantity < 1:
        raise ValueError("quantity must be >= 1")
    try:
        item = CartItem.objects.select_for_update().get(cart=cart, product_id=product_id)
    except CartItem.DoesNotExist:
        return cart
    if quantity >= item.quantity:
        item.delete()
    else:
        CartItem.objects.filter(pk=item.pk).update(quantity=F("quantity") - quantity)
    return cart


def serialize_cart(cart: Cart) -> dict:
    items = []
    total = Decimal("0")
    cart_items = cart.items.select_related("product").annotate(
        product_rating_avg=Avg("product__reviews__rating"),
        product_rating_count=Count("product__reviews"),
    )
    for it in cart_items:
        subtotal = it.product.price * it.quantity
        total += subtotal
        items.append(
            {
                "product_id": it.product_id,
                "name": it.product.name,
                "type": it.product.type,
                "subtype": it.product.subtype,
                "price": it.product.price,
                "quantity": it.quantity,
                "subtotal": subtotal,
                "image_url": it.product.image_url,
                "rating_avg": round(float(it.product_rating_avg or 0.0), 2),
                "rating_count": int(it.product_rating_count or 0),
            }
        )
    return {"items": items, "total": total}
