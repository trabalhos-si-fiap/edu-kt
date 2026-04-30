from decimal import Decimal

from ninja import Schema


class CartItemIn(Schema):
    product_id: int
    quantity: int = 1


class CartItemOut(Schema):
    product_id: int
    name: str
    price: Decimal
    quantity: int
    subtotal: Decimal


class CartOut(Schema):
    items: list[CartItemOut]
    total: Decimal
