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
def auth_headers(db, user):
    from apps.accounts.models import Token

    token = Token.objects.create(user=user)
    return {"Authorization": f"Bearer {token.key}"}
