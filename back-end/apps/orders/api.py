from ninja import Router

from apps.accounts.auth import bearer_auth
from apps.orders.models import Order
from apps.orders.schemas import OrderOut
from apps.orders.services import EmptyCartError, create_order_from_cart, serialize_order

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
    qs = Order.objects.filter(user=request.auth).prefetch_related("items")
    return [serialize_order(o) for o in qs]
