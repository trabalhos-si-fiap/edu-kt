import pytest

from apps.catalog.models import Product


@pytest.fixture
def product(db):
    return Product.objects.create(
        name="Apostila", type="APOSTILA", subtype="DIGITAL",
        description="d", price="10.00",
    )


@pytest.mark.django_db
def test_get_empty_cart(client, auth_headers):
    res = client.get("/cart", headers=auth_headers)
    assert res.status_code == 200
    body = res.json()
    assert body == {"items": [], "total": "0"} or body["items"] == []
    assert str(body["total"]) in ("0", "0.00")


@pytest.mark.django_db
def test_add_item_to_cart(client, auth_headers, product):
    res = client.post(
        "/cart/items",
        json={"product_id": product.id, "quantity": 2},
        headers=auth_headers,
    )
    assert res.status_code == 200
    body = res.json()
    assert len(body["items"]) == 1
    assert body["items"][0]["product_id"] == product.id
    assert body["items"][0]["quantity"] == 2
    assert body["items"][0]["type"] == "APOSTILA"
    assert body["items"][0]["subtype"] == "DIGITAL"
    assert str(body["total"]) == "20.00"


@pytest.mark.django_db
def test_add_existing_product_increments_quantity(client, auth_headers, product):
    client.post("/cart/items", json={"product_id": product.id, "quantity": 1}, headers=auth_headers)
    res = client.post(
        "/cart/items",
        json={"product_id": product.id, "quantity": 3},
        headers=auth_headers,
    )
    body = res.json()
    assert len(body["items"]) == 1
    assert body["items"][0]["quantity"] == 4
    assert str(body["total"]) == "40.00"


@pytest.mark.django_db
def test_add_unknown_product_returns_404(client, auth_headers):
    res = client.post(
        "/cart/items", json={"product_id": 9999, "quantity": 1}, headers=auth_headers
    )
    assert res.status_code == 404


@pytest.mark.django_db
def test_remove_item_from_cart(client, auth_headers, product):
    client.post("/cart/items", json={"product_id": product.id}, headers=auth_headers)
    res = client.delete(f"/cart/items/{product.id}", headers=auth_headers)
    assert res.status_code == 200
    assert res.json()["items"] == []


@pytest.mark.django_db
def test_remove_partial_quantity_decrements(client, auth_headers, product):
    client.post(
        "/cart/items", json={"product_id": product.id, "quantity": 5}, headers=auth_headers
    )
    res = client.delete(f"/cart/items/{product.id}?quantity=2", headers=auth_headers)
    assert res.status_code == 200
    body = res.json()
    assert body["items"][0]["quantity"] == 3
    assert str(body["total"]) == "30.00"


@pytest.mark.django_db
def test_remove_quantity_equal_or_greater_drops_item(client, auth_headers, product):
    client.post(
        "/cart/items", json={"product_id": product.id, "quantity": 2}, headers=auth_headers
    )
    res = client.delete(f"/cart/items/{product.id}?quantity=10", headers=auth_headers)
    assert res.status_code == 200
    assert res.json()["items"] == []


@pytest.mark.django_db
def test_remove_invalid_quantity_returns_400(client, auth_headers, product):
    client.post("/cart/items", json={"product_id": product.id}, headers=auth_headers)
    res = client.delete(f"/cart/items/{product.id}?quantity=0", headers=auth_headers)
    assert res.status_code == 400


@pytest.mark.django_db
def test_cart_is_isolated_per_user(client, auth_headers, product):
    from apps.accounts.models import Token, User

    other = User.objects.create_user(username="bob", email="bob@x.com", password="pw")
    other_headers = {"Authorization": f"Bearer {Token.objects.create(user=other).key}"}

    client.post("/cart/items", json={"product_id": product.id}, headers=auth_headers)
    res = client.get("/cart", headers=other_headers)
    assert res.json()["items"] == []
