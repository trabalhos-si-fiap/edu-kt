from datetime import datetime
from decimal import Decimal

from ninja import Schema


class ProductOut(Schema):
    id: int
    name: str
    type: str
    subtype: str
    description: str
    price: Decimal
    image_url: str
    rating_avg: float
    rating_count: int


class ProductListOut(Schema):
    items: list[ProductOut]
    total: int
    limit: int
    offset: int


class ReviewOut(Schema):
    id: int
    author: str
    rating: int
    comment: str
    created_at: datetime


class ReviewListOut(Schema):
    items: list[ReviewOut]
    total: int
    rating_avg: float
    rating_count: int


class ReviewIn(Schema):
    rating: int
    comment: str = ""
