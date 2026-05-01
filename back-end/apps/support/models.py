from django.conf import settings
from django.db import models


class SupportMessage(models.Model):
    SENDER_USER = "user"
    SENDER_SUPPORT = "support"
    SENDER_CHOICES = [
        (SENDER_USER, "User"),
        (SENDER_SUPPORT, "Support"),
    ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="support_messages",
    )
    sender = models.CharField(max_length=10, choices=SENDER_CHOICES)
    body = models.TextField(max_length=2000)
    created_at = models.DateTimeField(auto_now_add=True, db_index=True)

    class Meta:
        ordering = ["created_at"]
