from ninja import Router, Status

from apps.accounts.auth import jwt_auth
from apps.support.schemas import MessageIn, MessageOut
from apps.support.services import (
    list_messages,
    send_message,
    serialize_message,
)

router = Router(auth=jwt_auth)


@router.get("", response=list[MessageOut])
def get_messages(request, limit: int = 50):
    return [serialize_message(m) for m in list_messages(request.auth, limit=limit)]


@router.post("", response={201: list[MessageOut]})
def post_message(request, payload: MessageIn):
    user_msg, support_msg = send_message(request.auth, payload.body)
    return Status(201, [serialize_message(user_msg), serialize_message(support_msg)])
