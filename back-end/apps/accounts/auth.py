from ninja.security import HttpBearer

from apps.accounts.models import Token, User


class BearerAuth(HttpBearer):
    def authenticate(self, request, token: str) -> User | None:
        try:
            t = Token.objects.select_related("user").get(key=token)
        except (Token.DoesNotExist, ValueError):
            return None
        return t.user


bearer_auth = BearerAuth()
