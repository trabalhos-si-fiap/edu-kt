from decimal import Decimal

from django.db import transaction
from django.db.models import Avg, Count, F

from apps.cart.models import Cart, CartItem
from apps.cart.services import get_or_create_cart
from apps.orders.models import Order, OrderItem


class EmptyCartError(Exception):
    pass


@transaction.atomic
def create_order_from_cart(user) -> Order:
    cart = get_or_create_cart(user)
    items = list(cart.items.select_related("product"))
    if not items:
        raise EmptyCartError("Cart is empty")

    total = Decimal("0")
    for it in items:
        total += it.product.price * it.quantity

    order = Order.objects.create(user=user, total=total, payment_method="default")
    OrderItem.objects.bulk_create(
        [
            OrderItem(
                order=order,
                product=it.product,
                product_name=it.product.name,
                unit_price=it.product.price,
                quantity=it.quantity,
            )
            for it in items
        ]
    )
    cart.items.all().delete()
    return order


@transaction.atomic
def rebuy_order(user, order_id: int) -> Cart:
    order = Order.objects.filter(user=user, pk=order_id).first()
    if order is None:
        raise LookupError("Order not found")
    cart = get_or_create_cart(user)
    for it in order.items.select_related("product"):
        existing = CartItem.objects.filter(cart=cart, product=it.product).first()
        if existing is None:
            CartItem.objects.create(cart=cart, product=it.product, quantity=it.quantity)
        else:
            CartItem.objects.filter(pk=existing.pk).update(
                quantity=F("quantity") + it.quantity
            )
    return cart


def serialize_order(order: Order) -> dict:
    items_qs = order.items.select_related("product").annotate(
        rating_avg=Avg("product__reviews__rating"),
        rating_count=Count("product__reviews"),
    )
    return {
        "id": order.id,
        "total": order.total,
        "payment_method": order.payment_method,
        "created_at": order.created_at,
        "items": [
            {
                "product_id": i.product_id,
                "product_name": i.product_name,
                "unit_price": i.unit_price,
                "quantity": i.quantity,
                "image_url": i.product.image_url if i.product else "",
                "rating_avg": round(float(getattr(i, "rating_avg", None) or 0.0), 2),
                "rating_count": int(getattr(i, "rating_count", 0) or 0),
            }
            for i in items_qs
        ],
    }
