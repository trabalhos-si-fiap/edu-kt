import pytest

from apps.catalog.models import Product, Review


@pytest.mark.django_db
def test_get_products_returns_seeded_fields(client):
    Product.objects.create(
        name="Apostila", type="APOSTILA", subtype="DIGITAL",
        description="desc", price="49.90",
        image_url="https://cdn.example.com/apostila.png",
    )
    res = client.get("/products")
    assert res.status_code == 200
    body = res.json()
    assert body["total"] == 1
    assert body["limit"] == 20
    assert body["offset"] == 0
    assert len(body["items"]) == 1
    p = body["items"][0]
    assert {
        "id", "name", "type", "subtype", "description", "price", "image_url",
        "rating_avg", "rating_count",
    } <= set(p.keys())
    assert p["rating_avg"] == 0
    assert p["rating_count"] == 0
    assert p["name"] == "Apostila"
    assert p["type"] == "APOSTILA"
    assert p["subtype"] == "DIGITAL"
    assert str(p["price"]) == "49.90"
    assert p["image_url"] == "https://cdn.example.com/apostila.png"


@pytest.mark.django_db
def test_get_products_image_url_defaults_to_empty(client):
    Product.objects.create(
        name="Sem Imagem", type="APOSTILA", subtype="DIGITAL",
        description="desc", price="10.00",
    )
    res = client.get("/products")
    assert res.status_code == 200
    assert res.json()["items"][0]["image_url"] == ""


@pytest.mark.django_db
def test_get_products_empty(client):
    res = client.get("/products")
    assert res.status_code == 200
    body = res.json()
    assert body["items"] == []
    assert body["total"] == 0


@pytest.mark.django_db
def test_get_products_search_filters_by_name(client):
    Product.objects.create(
        name="Apostila Matemática", type="APOSTILA", subtype="DIGITAL",
        description="algebra", price="10.00",
    )
    Product.objects.create(
        name="Curso de Português", type="CURSO", subtype="ONLINE",
        description="gramatica", price="20.00",
    )
    res = client.get("/products?q=matem")
    assert res.status_code == 200
    body = res.json()
    assert body["total"] == 1
    assert body["items"][0]["name"] == "Apostila Matemática"


@pytest.mark.django_db
def test_get_products_search_matches_description(client):
    Product.objects.create(
        name="Produto A", type="APOSTILA", subtype="DIGITAL",
        description="trigonometria avançada", price="10.00",
    )
    Product.objects.create(
        name="Produto B", type="CURSO", subtype="ONLINE",
        description="introdução", price="20.00",
    )
    res = client.get("/products?q=trigon")
    body = res.json()
    assert body["total"] == 1
    assert body["items"][0]["name"] == "Produto A"


@pytest.mark.django_db
def test_get_products_pagination(client):
    for i in range(5):
        Product.objects.create(
            name=f"Item {i}", type="APOSTILA", subtype="DIGITAL",
            description="d", price="1.00",
        )
    res = client.get("/products?limit=2&offset=2")
    assert res.status_code == 200
    body = res.json()
    assert body["total"] == 5
    assert body["limit"] == 2
    assert body["offset"] == 2
    assert len(body["items"]) == 2
    assert body["items"][0]["name"] == "Item 2"
    assert body["items"][1]["name"] == "Item 3"


@pytest.mark.django_db
def test_get_products_limit_validation(client):
    res = client.get("/products?limit=0")
    assert res.status_code == 422
    res = client.get("/products?limit=1000")
    assert res.status_code == 422


@pytest.mark.django_db
def test_get_products_includes_rating_aggregates(client):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    Review.objects.create(product=product, author="A", rating=5, comment="ótimo")
    Review.objects.create(product=product, author="B", rating=3, comment="ok")
    res = client.get("/products")
    body = res.json()
    p = body["items"][0]
    assert p["rating_count"] == 2
    assert p["rating_avg"] == 4.0


@pytest.mark.django_db
def test_get_product_returns_single_product(client):
    product = Product.objects.create(
        name="Resumo Estratégico - Filosofia Moderna",
        type="EBOOK",
        subtype="RESUMO_DA_MATERIA",
        description="Descartes, Hume, Kant",
        price="15.90",
        image_url="https://cdn.example.com/filo.png",
    )
    Review.objects.create(product=product, author="A", rating=5, comment="ok")
    Review.objects.create(product=product, author="B", rating=4, comment="bom")

    res = client.get(f"/products/{product.id}")

    assert res.status_code == 200
    body = res.json()
    assert body["id"] == product.id
    assert body["name"] == "Resumo Estratégico - Filosofia Moderna"
    assert body["type"] == "EBOOK"
    assert body["subtype"] == "RESUMO_DA_MATERIA"
    assert body["description"] == "Descartes, Hume, Kant"
    assert str(body["price"]) == "15.90"
    assert body["image_url"] == "https://cdn.example.com/filo.png"
    assert body["rating_count"] == 2
    assert body["rating_avg"] == 4.5


@pytest.mark.django_db
def test_get_product_works_for_id_beyond_default_list_limit(client):
    last = None
    for i in range(25):
        last = Product.objects.create(
            name=f"P{i}", type="EBOOK", subtype="RESUMO",
            description="d", price="1.00",
        )
    res = client.get(f"/products/{last.id}")
    assert res.status_code == 200
    assert res.json()["id"] == last.id


@pytest.mark.django_db
def test_get_product_404_for_unknown_id(client):
    res = client.get("/products/9999")
    assert res.status_code == 404


@pytest.mark.django_db
def test_get_product_reviews(client):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    Review.objects.create(product=product, author="Ana", rating=5, comment="excelente")
    Review.objects.create(product=product, author="Bruno", rating=4, comment="bom")
    res = client.get(f"/products/{product.id}/reviews")
    assert res.status_code == 200
    body = res.json()
    assert body["total"] == 2
    assert body["rating_count"] == 2
    assert body["rating_avg"] == 4.5
    assert len(body["items"]) == 2
    item = body["items"][0]
    assert {"id", "author", "rating", "comment", "created_at"} <= set(item.keys())


@pytest.mark.django_db
def test_get_product_reviews_404_for_unknown_product(client):
    res = client.get("/products/9999/reviews")
    assert res.status_code == 404


@pytest.mark.django_db
def test_get_product_reviews_empty(client):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    res = client.get(f"/products/{product.id}/reviews")
    body = res.json()
    assert body["total"] == 0
    assert body["rating_count"] == 0
    assert body["rating_avg"] == 0
    assert body["items"] == []


@pytest.mark.django_db
def test_get_categories_groups_by_type(client):
    Product.objects.create(
        name="A", type="EBOOK", subtype="RESUMO",
        description="d", price="1.00",
    )
    Product.objects.create(
        name="B", type="EBOOK", subtype="GUIA",
        description="d", price="1.00",
    )
    Product.objects.create(
        name="C", type="CURSO", subtype="ONLINE",
        description="d", price="1.00",
    )
    Product.objects.create(
        name="D", type="", subtype="",
        description="d", price="1.00",
    )

    res = client.get("/products/categories")

    assert res.status_code == 200
    items = res.json()["items"]
    assert items == [
        {"type": "CURSO", "count": 1},
        {"type": "EBOOK", "count": 2},
    ]


@pytest.mark.django_db
def test_create_review_persists_review(client, user, auth_headers):
    user.first_name = "Alice"
    user.last_name = "Liddell"
    user.save()
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )

    res = client.post(
        f"/products/{product.id}/reviews",
        json={"rating": 5, "comment": "ótimo"},
        headers=auth_headers,
    )

    assert res.status_code == 201
    body = res.json()
    assert body["rating"] == 5
    assert body["comment"] == "ótimo"
    assert body["author"] == "Alice Liddell"
    assert Review.objects.filter(product=product, rating=5).count() == 1


@pytest.mark.django_db
def test_create_review_falls_back_to_email_then_anon(client, user, auth_headers):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    res = client.post(
        f"/products/{product.id}/reviews",
        json={"rating": 4, "comment": ""},
        headers=auth_headers,
    )
    assert res.status_code == 201
    assert res.json()["author"] == "alice@example.com"

    user.email = ""
    user.save()
    res = client.post(
        f"/products/{product.id}/reviews",
        json={"rating": 3, "comment": ""},
        headers=auth_headers,
    )
    assert res.status_code == 201
    assert res.json()["author"] == "Anônimo"


@pytest.mark.django_db
def test_create_review_truncates_long_comment(client, user, auth_headers):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    long_comment = "x" * 1500
    res = client.post(
        f"/products/{product.id}/reviews",
        json={"rating": 5, "comment": long_comment},
        headers=auth_headers,
    )
    assert res.status_code == 201
    review = Review.objects.get(pk=res.json()["id"])
    assert len(review.comment) == 1000


@pytest.mark.django_db
def test_create_review_rejects_out_of_range_rating(client, user, auth_headers):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    for bad in (0, 6):
        res = client.post(
            f"/products/{product.id}/reviews",
            json={"rating": bad, "comment": "x"},
            headers=auth_headers,
        )
        assert res.status_code == 400
        assert res.json()["detail"] == "rating must be between 1 and 5"
    assert Review.objects.count() == 0


@pytest.mark.django_db
def test_create_review_404_for_unknown_product(client, user, auth_headers):
    res = client.post(
        "/products/9999/reviews",
        json={"rating": 5, "comment": "x"},
        headers=auth_headers,
    )
    assert res.status_code == 404


@pytest.mark.django_db
def test_create_review_requires_auth(client):
    product = Product.objects.create(
        name="Curso", type="CURSO", subtype="ONLINE",
        description="d", price="10.00",
    )
    res = client.post(
        f"/products/{product.id}/reviews",
        json={"rating": 5, "comment": "x"},
    )
    assert res.status_code == 401
