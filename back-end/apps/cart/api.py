from ninja import Router

from apps.accounts.auth import bearer_auth
from apps.cart.schemas import CartItemIn, CartOut
from apps.cart.services import add_item, get_or_create_cart, remove_item, serialize_cart

router = Router(auth=bearer_auth)


@router.get("", response=CartOut)
def get_cart(request):
    cart = get_or_create_cart(request.auth)
    return serialize_cart(cart)


@router.post("/items", response={200: CartOut, 404: dict, 400: dict})
def add_cart_item(request, payload: CartItemIn):
    try:
        cart = add_item(request.auth, payload.product_id, payload.quantity)
    except LookupError:
        return 404, {"detail": "Product not found"}
    except ValueError as e:
        return 400, {"detail": str(e)}
    return 200, serialize_cart(cart)


@router.delete("/items/{product_id}", response={200: CartOut, 400: dict})
def remove_cart_item(request, product_id: int, quantity: int | None = None):
    try:
        cart = remove_item(request.auth, product_id, quantity)
    except ValueError as e:
        return 400, {"detail": str(e)}
    return 200, serialize_cart(cart)
