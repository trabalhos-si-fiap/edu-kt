from ninja.security import APIKeyHeader

from apps.accounts.models import Token, User


class BearerAuth(APIKeyHeader):
    param_name = "Authorization"
    openapi_scheme = "bearer"

    def authenticate(self, request, key: str | None) -> User | None:
        if not key:
            return None
        token_value = key.split()[-1].strip()
        if not token_value:
            return None
        try:
            t = Token.objects.select_related("user").get(key=token_value)
        except (Token.DoesNotExist, ValueError):
            return None
        return t.user


bearer_auth = BearerAuth()
