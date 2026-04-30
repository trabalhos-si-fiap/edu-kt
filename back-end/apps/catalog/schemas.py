from decimal import Decimal

from ninja import Schema


class ProductOut(Schema):
    id: int
    name: str
    type: str
    subtype: str
    description: str
    price: Decimal
