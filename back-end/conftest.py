import pytest
from ninja.testing import TestClient

from config.api import api


@pytest.fixture
def client() -> TestClient:
    return TestClient(api)


@pytest.fixture
def user(db):
    from apps.accounts.models import User

    return User.objects.create_user(
        username="alice", email="alice@example.com", password="pw12345!"
    )


@pytest.fixture
def token_pair(db, user):
    from ninja_jwt.tokens import RefreshToken

    refresh = RefreshToken.for_user(user)
    return {"access": str(refresh.access_token), "refresh": str(refresh)}


@pytest.fixture
def auth_headers(token_pair):
    return {"Authorization": f"Bearer {token_pair['access']}"}
