import pytest

from apps.accounts.models import Token, User


@pytest.mark.django_db
def test_register_returns_token(client):
    res = client.post(
        "/auth/register",
        json={"email": "bob@example.com", "password": "pw12345!", "name": "Bob"},
    )
    assert res.status_code == 201
    body = res.json()
    assert body["email"] == "bob@example.com"
    assert body["token"]
    assert User.objects.filter(email="bob@example.com").exists()
    assert Token.objects.filter(key=body["token"]).exists()


@pytest.mark.django_db
def test_register_duplicate_email_returns_400(client):
    client.post("/auth/register", json={"email": "x@y.com", "password": "pw12345!"})
    res = client.post("/auth/register", json={"email": "x@y.com", "password": "pw12345!"})
    assert res.status_code == 400


@pytest.mark.django_db
def test_login_with_valid_credentials(client):
    User.objects.create_user(username="a@b.com", email="a@b.com", password="pw12345!")
    res = client.post("/auth/login", json={"email": "a@b.com", "password": "pw12345!"})
    assert res.status_code == 200
    assert res.json()["token"]


@pytest.mark.django_db
def test_login_with_bad_password_returns_401(client):
    User.objects.create_user(username="a@b.com", email="a@b.com", password="pw12345!")
    res = client.post("/auth/login", json={"email": "a@b.com", "password": "wrong"})
    assert res.status_code == 401


@pytest.mark.django_db
def test_protected_endpoint_requires_token(client):
    res = client.get("/cart")
    assert res.status_code == 401
