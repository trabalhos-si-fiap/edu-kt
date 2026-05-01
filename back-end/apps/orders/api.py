from ninja import Router

from apps.accounts.auth import bearer_auth
from apps.cart.services import serialize_cart
from apps.orders.models import Order
from apps.orders.schemas import OrderOut
from apps.orders.services import (
    EmptyCartError,
    create_order_from_cart,
    rebuy_order,
    serialize_order,
)

router = Router(auth=bearer_auth)


@router.post("", response={201: OrderOut, 400: dict})
def create_order(request):
    try:
        order = create_order_from_cart(request.auth)
    except EmptyCartError as e:
        return 400, {"detail": str(e)}
    return 201, serialize_order(order)


@router.get("", response=list[OrderOut])
def list_orders(request):
    qs = Order.objects.filter(user=request.auth).prefetch_related("items__product")
    return [serialize_order(o) for o in qs]


@router.post("/{order_id}/rebuy", response={200: dict, 404: dict})
def rebuy(request, order_id: int):
    try:
        cart = rebuy_order(request.auth, order_id)
    except LookupError:
        return 404, {"detail": "Order not found"}
    return 200, serialize_cart(cart)
