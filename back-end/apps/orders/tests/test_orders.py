import pytest

from apps.catalog.models import Product


@pytest.fixture
def product(db):
    return Product.objects.create(
        name="Apostila", type="APOSTILA", subtype="DIGITAL",
        description="d", price="10.00",
    )


@pytest.mark.django_db
def test_create_order_from_cart_persists_and_clears_cart(client, auth_headers, product):
    client.post(
        "/cart/items", json={"product_id": product.id, "quantity": 2}, headers=auth_headers
    )
    res = client.post("/orders", headers=auth_headers)
    assert res.status_code == 201
    body = res.json()
    assert str(body["total"]) == "20.00"
    assert body["payment_method"] == "default"
    assert len(body["items"]) == 1
    assert body["items"][0]["product_name"] == "Apostila"
    assert str(body["items"][0]["unit_price"]) == "10.00"
    assert body["items"][0]["quantity"] == 2

    cart = client.get("/cart", headers=auth_headers).json()
    assert cart["items"] == []


@pytest.mark.django_db
def test_create_order_with_empty_cart_returns_400(client, auth_headers):
    res = client.post("/orders", headers=auth_headers)
    assert res.status_code == 400
    assert "empty" in res.json()["detail"].lower()


@pytest.mark.django_db
def test_total_uses_backend_price_not_client(client, auth_headers, product):
    client.post(
        "/cart/items",
        json={"product_id": product.id, "quantity": 1, "price": "0.01"},
        headers=auth_headers,
    )
    body = client.post("/orders", headers=auth_headers).json()
    assert str(body["total"]) == "10.00"


@pytest.mark.django_db
def test_list_orders_returns_user_history(client, auth_headers, product):
    client.post("/cart/items", json={"product_id": product.id}, headers=auth_headers)
    client.post("/orders", headers=auth_headers)
    res = client.get("/orders", headers=auth_headers)
    assert res.status_code == 200
    assert len(res.json()) == 1
