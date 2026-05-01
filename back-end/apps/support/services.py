from django.db import transaction

from apps.support.models import SupportMessage

AUTO_REPLY = (
    "Recebemos sua mensagem. Um mentor responderá em breve. "
    "Enquanto isso, confira o status na tela de Pedidos."
)


def list_messages(user, limit: int = 50) -> list[SupportMessage]:
    qs = SupportMessage.objects.filter(user=user).order_by("created_at")
    return list(qs[:limit])


@transaction.atomic
def send_message(user, body: str) -> tuple[SupportMessage, SupportMessage]:
    user_msg = SupportMessage.objects.create(
        user=user, sender=SupportMessage.SENDER_USER, body=body
    )
    support_msg = SupportMessage.objects.create(
        user=user, sender=SupportMessage.SENDER_SUPPORT, body=AUTO_REPLY
    )
    return user_msg, support_msg


def serialize_message(m: SupportMessage) -> dict:
    return {
        "id": m.id,
        "sender": m.sender,
        "body": m.body,
        "created_at": m.created_at,
    }
