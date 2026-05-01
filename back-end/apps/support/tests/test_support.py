import pytest

from apps.accounts.models import User
from apps.support.models import SupportMessage


@pytest.mark.django_db
def test_list_messages_empty_returns_empty_list(client, auth_headers):
    res = client.get("/support", headers=auth_headers)
    assert res.status_code == 200
    assert res.json() == []


@pytest.mark.django_db
def test_list_messages_requires_auth(client):
    res = client.get("/support")
    assert res.status_code == 401


@pytest.mark.django_db
def test_send_message_persists_user_and_support_reply(client, user, auth_headers):
    res = client.post(
        "/support",
        json={"body": "Onde está meu kit acadêmico?"},
        headers=auth_headers,
    )
    assert res.status_code == 201
    body = res.json()
    assert len(body) == 2
    assert body[0]["sender"] == "user"
    assert body[0]["body"] == "Onde está meu kit acadêmico?"
    assert body[1]["sender"] == "support"
    assert body[1]["body"]
    assert SupportMessage.objects.filter(user=user).count() == 2


@pytest.mark.django_db
def test_send_message_requires_auth(client):
    res = client.post("/support", json={"body": "hi"})
    assert res.status_code == 401


@pytest.mark.django_db
def test_list_messages_only_returns_own(client, user, auth_headers):
    other = User.objects.create_user(username="bob", email="bob@example.com", password="pw12345!")
    SupportMessage.objects.create(user=other, sender="user", body="not mine")
    SupportMessage.objects.create(user=user, sender="user", body="mine")

    res = client.get("/support", headers=auth_headers)
    assert res.status_code == 200
    bodies = [m["body"] for m in res.json()]
    assert bodies == ["mine"]


@pytest.mark.django_db
def test_send_message_rejects_empty_body(client, auth_headers):
    res = client.post("/support", json={"body": ""}, headers=auth_headers)
    assert res.status_code == 422


@pytest.mark.django_db
def test_send_message_rejects_oversized_body(client, auth_headers):
    res = client.post("/support", json={"body": "x" * 2001}, headers=auth_headers)
    assert res.status_code == 422


@pytest.mark.django_db
def test_messages_returned_in_chronological_order(client, user, auth_headers):
    SupportMessage.objects.create(user=user, sender="user", body="first")
    SupportMessage.objects.create(user=user, sender="support", body="second")
    SupportMessage.objects.create(user=user, sender="user", body="third")

    res = client.get("/support", headers=auth_headers)
    assert res.status_code == 200
    bodies = [m["body"] for m in res.json()]
    assert bodies == ["first", "second", "third"]


@pytest.mark.django_db
def test_send_message_returns_messages_with_ids_and_timestamps(client, auth_headers):
    res = client.post("/support", json={"body": "hi"}, headers=auth_headers)
    assert res.status_code == 201
    for m in res.json():
        assert isinstance(m["id"], int)
        assert m["created_at"]
