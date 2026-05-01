from decimal import Decimal

from ninja import Schema


class CartItemIn(Schema):
    product_id: int
    quantity: int = 1


class CartItemOut(Schema):
    product_id: int
    name: str
    type: str
    subtype: str
    price: Decimal
    quantity: int
    subtotal: Decimal
    image_url: str = ""
    rating_avg: float = 0.0
    rating_count: int = 0


class CartOut(Schema):
    items: list[CartItemOut]
    total: Decimal
