from datetime import datetime
from decimal import Decimal

from ninja import Schema


class OrderItemOut(Schema):
    product_id: int
    product_name: str
    unit_price: Decimal
    quantity: int


class OrderOut(Schema):
    id: int
    total: Decimal
    payment_method: str
    created_at: datetime
    items: list[OrderItemOut]
