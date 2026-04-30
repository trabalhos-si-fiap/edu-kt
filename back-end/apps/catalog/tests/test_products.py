import pytest

from apps.catalog.models import Product


@pytest.mark.django_db
def test_get_products_returns_seeded_fields(client):
    Product.objects.create(
        name="Apostila", type="APOSTILA", subtype="DIGITAL",
        description="desc", price="49.90",
    )
    res = client.get("/products")
    assert res.status_code == 200
    body = res.json()
    assert len(body) == 1
    p = body[0]
    assert {"id", "name", "type", "subtype", "description", "price"} <= set(p.keys())
    assert p["name"] == "Apostila"
    assert p["type"] == "APOSTILA"
    assert p["subtype"] == "DIGITAL"
    assert str(p["price"]) == "49.90"


@pytest.mark.django_db
def test_get_products_empty(client):
    res = client.get("/products")
    assert res.status_code == 200
    assert res.json() == []
