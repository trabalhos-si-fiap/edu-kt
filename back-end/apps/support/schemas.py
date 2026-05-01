from datetime import datetime

from ninja import Field, Schema


class MessageOut(Schema):
    id: int
    sender: str
    body: str
    created_at: datetime


class MessageIn(Schema):
    body: str = Field(min_length=1, max_length=2000)
