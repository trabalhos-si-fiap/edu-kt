from django.core.management.base import BaseCommand

from apps.accounts.models import User

DEFAULT_USERNAME = "admin"
DEFAULT_EMAIL = "admin@admin.local"
DEFAULT_PASSWORD = "admin"


class Command(BaseCommand):
    help = "Idempotente: cria/atualiza superusuário padrão admin/admin para dev."

    def handle(self, *args, **options) -> None:
        user, created = User.objects.update_or_create(
            username=DEFAULT_USERNAME,
            defaults={
                "email": DEFAULT_EMAIL,
                "is_staff": True,
                "is_superuser": True,
                "is_active": True,
            },
        )
        user.set_password(DEFAULT_PASSWORD)
        user.save(update_fields=["password"])
        verb = "created" if created else "updated"
        self.stdout.write(
            self.style.SUCCESS(
                f"Superuser {verb} — username={DEFAULT_USERNAME} email={DEFAULT_EMAIL}"
            )
        )
