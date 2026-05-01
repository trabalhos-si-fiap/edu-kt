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


@pytest.mark.django_db
def test_register_persists_name_phone_birth_date(client):
    res = client.post(
        "/auth/register",
        json={
            "email": "joao@example.com",
            "password": "pw12345!",
            "name": "João Silva",
            "phone": "11999998888",
            "birth_date": "1990-05-12",
        },
    )
    assert res.status_code == 201
    user = User.objects.get(email="joao@example.com")
    assert user.first_name == "João"
    assert user.last_name == "Silva"
    assert user.phone == "11999998888"
    assert user.birth_date.isoformat() == "1990-05-12"


@pytest.mark.django_db
def test_register_invalid_birth_date_returns_400(client):
    res = client.post(
        "/auth/register",
        json={
            "email": "bad@example.com",
            "password": "pw12345!",
            "birth_date": "not-a-date",
        },
    )
    assert res.status_code == 400


@pytest.mark.django_db
def test_get_me_returns_current_user(client, user, auth_headers):
    user.first_name = "Alice"
    user.last_name = "Wonder"
    user.phone = "11900000000"
    user.save()
    res = client.get("/auth/me", headers=auth_headers)
    assert res.status_code == 200
    body = res.json()
    assert body["email"] == user.email
    assert body["name"] == "Alice Wonder"
    assert body["phone"] == "11900000000"
    assert body["birth_date"] is None


@pytest.mark.django_db
def test_get_me_unauthenticated_returns_401(client):
    res = client.get("/auth/me")
    assert res.status_code == 401


@pytest.mark.django_db
def test_patch_me_updates_fields(client, user, auth_headers):
    res = client.patch(
        "/auth/me",
        json={"name": "New Name", "phone": "21988887777", "birth_date": "1995-01-02"},
        headers=auth_headers,
    )
    assert res.status_code == 200
    body = res.json()
    assert body["name"] == "New Name"
    assert body["phone"] == "21988887777"
    assert body["birth_date"] == "1995-01-02"

    user.refresh_from_db()
    assert user.first_name == "New"
    assert user.last_name == "Name"
    assert user.phone == "21988887777"
    assert user.birth_date.isoformat() == "1995-01-02"


@pytest.mark.django_db
def test_patch_me_invalid_birth_date_returns_400(client, auth_headers):
    res = client.patch(
        "/auth/me",
        json={"birth_date": "not-a-date"},
        headers=auth_headers,
    )
    assert res.status_code == 400


@pytest.mark.django_db
def test_patch_me_clears_birth_date_with_empty_string(client, user, auth_headers):
    user.birth_date = "1990-01-01"
    user.save()
    res = client.patch("/auth/me", json={"birth_date": ""}, headers=auth_headers)
    assert res.status_code == 200
    user.refresh_from_db()
    assert user.birth_date is None
