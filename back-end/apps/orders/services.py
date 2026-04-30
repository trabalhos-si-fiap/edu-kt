from decimal import Decimal

from django.db import transaction

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


def serialize_order(order: Order) -> dict:
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
            }
            for i in order.items.all()
        ],
    }
