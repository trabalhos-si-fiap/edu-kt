import pytest

from apps.accounts.models import Address, Token, User


def _payload(**overrides):
    base = {
        "label": "Casa",
        "zip_code": "01310-100",
        "street": "Av Paulista",
        "number": "1000",
        "complement": "Apto 12",
        "neighborhood": "Bela Vista",
        "city": "São Paulo",
        "state": "SP",
        "is_favorite": False,
    }
    base.update(overrides)
    return base


@pytest.mark.django_db
def test_list_addresses_empty(client, auth_headers):
    res = client.get("/auth/addresses", headers=auth_headers)
    assert res.status_code == 200
    assert res.json() == []


@pytest.mark.django_db
def test_create_address(client, user, auth_headers):
    res = client.post("/auth/addresses", json=_payload(), headers=auth_headers)
    assert res.status_code == 201
    body = res.json()
    assert body["street"] == "Av Paulista"
    assert body["is_favorite"] is False
    assert Address.objects.filter(user=user).count() == 1


@pytest.mark.django_db
def test_list_after_create(client, auth_headers):
    client.post("/auth/addresses", json=_payload(label="A"), headers=auth_headers)
    client.post(
        "/auth/addresses", json=_payload(label="B", is_favorite=True), headers=auth_headers
    )
    res = client.get("/auth/addresses", headers=auth_headers)
    assert res.status_code == 200
    body = res.json()
    assert len(body) == 2
    assert body[0]["label"] == "B"
    assert body[0]["is_favorite"] is True


@pytest.mark.django_db
def test_patch_address(client, auth_headers):
    created = client.post(
        "/auth/addresses", json=_payload(), headers=auth_headers
    ).json()
    res = client.patch(
        f"/auth/addresses/{created['id']}",
        json={"label": "Trabalho", "number": "2000"},
        headers=auth_headers,
    )
    assert res.status_code == 200
    body = res.json()
    assert body["label"] == "Trabalho"
    assert body["number"] == "2000"
    assert body["street"] == "Av Paulista"


@pytest.mark.django_db
def test_promote_to_favorite_demotes_previous(client, user, auth_headers):
    a = client.post(
        "/auth/addresses", json=_payload(label="A", is_favorite=True), headers=auth_headers
    ).json()
    b = client.post(
        "/auth/addresses", json=_payload(label="B"), headers=auth_headers
    ).json()
    res = client.patch(
        f"/auth/addresses/{b['id']}", json={"is_favorite": True}, headers=auth_headers
    )
    assert res.status_code == 200
    assert res.json()["is_favorite"] is True

    favs = Address.objects.filter(user=user, is_favorite=True)
    assert favs.count() == 1
    assert favs.first().id == b["id"]
    assert Address.objects.get(id=a["id"]).is_favorite is False


@pytest.mark.django_db
def test_cannot_access_other_users_address(client, auth_headers):
    other = User.objects.create_user(
        username="other", email="other@example.com", password="pw12345!"
    )
    other_token = Token.objects.create(user=other)
    other_headers = {"Authorization": f"Bearer {other_token.key}"}
    other_addr = client.post(
        "/auth/addresses", json=_payload(), headers=other_headers
    ).json()

    res = client.patch(
        f"/auth/addresses/{other_addr['id']}",
        json={"label": "Hack"},
        headers=auth_headers,
    )
    assert res.status_code == 404

    res = client.delete(f"/auth/addresses/{other_addr['id']}", headers=auth_headers)
    assert res.status_code == 404


@pytest.mark.django_db
def test_delete_favorite_then_create_new_favorite(client, user, auth_headers):
    a = client.post(
        "/auth/addresses", json=_payload(is_favorite=True), headers=auth_headers
    ).json()
    res = client.delete(f"/auth/addresses/{a['id']}", headers=auth_headers)
    assert res.status_code == 204

    res = client.post(
        "/auth/addresses", json=_payload(label="New", is_favorite=True), headers=auth_headers
    )
    assert res.status_code == 201
    assert Address.objects.filter(user=user, is_favorite=True).count() == 1
