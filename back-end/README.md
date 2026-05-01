# Edu Backend — Marketplace Orders API

Django + Django Ninja + SQLite. Serve JSON para o app Kotlin.

## Stack

- Python 3.12 · Django 6 · django-ninja
- SQLite (dev/local)
- Granian (ASGI server) com hot reload
- Pydantic via Ninja Schemas
- WhiteNoise (serve estáticos do admin sob Granian)
- uv para gerenciamento de pacotes

## Setup local

```bash
uv sync
uv run python manage.py migrate
uv run python manage.py collectstatic --noinput
uv run python manage.py seed_catalog
uv run python manage.py seed_admin
uv run granian --interface asgi config.asgi:application --host 0.0.0.0 --port 8000 --reload
```

API em `http://localhost:8000/api/` · Docs OpenAPI em `http://localhost:8000/api/docs` ·
Django admin em `http://localhost:8000/admin/`.

> `collectstatic` só é necessário sob Granian/produção. Sob `runserver` (DEBUG=1) o
> WhiteNoise serve direto da árvore de origem via `whitenoise.runserver_nostatic`.

## Docker

```bash
docker compose up --build
```

Dois serviços:

- **`migrate`** — one-shot. Roda `manage.py migrate` + `seed_catalog` + `seed_admin` e termina.
- **`api`** — sobe Granian com `--reload`, após `collectstatic --noinput`. Depende de `migrate` finalizar com sucesso (`service_completed_successfully`).

O código do host é montado em `/app` (hot reload), e o SQLite persiste em `./data/db.sqlite3`.

Rodar só as migrations / seed manualmente:

```bash
docker compose run --rm migrate
```

## Testes

```bash
uv run pytest
uv run ruff check .
```

## Estrutura

```
back-end/
├── config/                     # Django project (settings, urls, asgi, NinjaAPI)
├── apps/
│   ├── accounts/               # User + Token bearer auth
│   ├── catalog/                # Product + Review + GET /products, /products/{id}, /products/{id}/reviews
│   │   └── management/commands/seed_catalog.py
│   ├── cart/                   # Cart, CartItem, services, /cart endpoints
│   └── orders/                 # Order atomic creation, /orders endpoints
├── Dockerfile
├── docker-compose.yml
└── pyproject.toml
```

## Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | — | `{email, password, name?}` → `{token, email}` |
| POST | `/api/auth/login` | — | `{email, password}` → `{token, email}` |
| GET | `/api/products` | — | Lista catálogo paginado. Query params: `q` (busca em `name`/`description`/`type`/`subtype`, `icontains`), `limit` (1–100, default 20), `offset` (≥0, default 0). Resposta: `{items: [...], total, limit, offset}`. Cada item inclui `rating_avg` (float 0–5) e `rating_count` (int) agregados via `Avg/Count` em `reviews`. |
| GET | `/api/products/{id}` | — | Detalhe de um produto com `rating_avg`/`rating_count` agregados. Mesmo shape do item de `/api/products`. 404 se não existir. Usado pela `ProductDetailScreen` no app, que precisa abrir produtos por id sem depender da página atual da listagem. |
| GET | `/api/products/categories` | — | Tipos distintos do catálogo (apenas `type` não vazio) com `count` por tipo, ordenados alfabeticamente. Resposta: `{items: [{type, count}]}`. |
| GET | `/api/products/{id}/reviews` | — | Avaliações do produto, ordenadas por `created_at desc`. Query params: `limit` (1–100, default 20), `offset` (≥0, default 0). Resposta: `{items: [{id, author, rating, comment, created_at}], total, rating_avg, rating_count}`. 404 se produto não existir. |
| POST | `/api/products/{id}/reviews` | bearer | `{rating: 1..5, comment?}` cria nova `Review`. Autor preenchido com `user.get_full_name()` ou `user.email` (truncado em 120 chars; "Anônimo" como fallback). 400 se `rating` fora de 1–5; 404 se produto não existir. |
| GET | `/api/cart` | bearer | Itens (`product_id, name, type, subtype, price, quantity, subtotal`) + total |
| POST | `/api/cart/items` | bearer | `{product_id, quantity}` — incrementa se já existir |
| DELETE | `/api/cart/items/{product_id}?quantity=N` | bearer | Remove item; com `quantity` decrementa (remove se ≥ atual); sem `quantity` apaga o item inteiro |
| POST | `/api/orders` | bearer | Cria pedido a partir do carrinho e o esvazia |
| GET | `/api/orders` | bearer | Histórico de pedidos do usuário. Cada `OrderItemOut` inclui `image_url`, `rating_avg`, `rating_count` (agregados via `Avg/Count` em `product__reviews`) — usado pelo carrossel da `OrdersScreen`. |
| POST | `/api/orders/{id}/rebuy` | bearer | "Comprar novamente": copia os itens do pedido para o carrinho do usuário, incrementando quantidades se o produto já estiver no carrinho. Atômico (`@transaction.atomic`). Retorna `CartOut` atualizado. 404 se pedido não pertencer ao usuário. |

## Regras de negócio implementadas

| RN | Garantia |
|---|---|
| RN01 | `GET /products` retorna `{items, total, limit, offset}`, com cada item contendo `name, type, subtype, description, price, image_url, rating_avg, rating_count`. Suporta busca via `?q=` e paginação `?limit=&offset=`. |
| RN01a | `GET /products/{id}/reviews` retorna avaliações ordenadas por `created_at desc`, com agregados `rating_avg`/`rating_count`. `Review.rating` validado em 1–5 (`MinValueValidator`/`MaxValueValidator`); `comment` limitado a 1000 chars. |
| RN02–RN04 | Endpoints de carrinho cobrindo add / remove / list. |
| RN05 | `total` calculado server-side em cada resposta de `/cart`. |
| RN06 | `add_item` faz `get_or_create` + `F("quantity") + n` (atômico). |
| RN07–RN08 | `POST /orders` persiste lista de itens, total e `created_at`. |
| RN09 | Após criar pedido, `cart.items.all().delete()` na mesma transação. |
| RN10 | Produto inexistente → 404. |
| RN11 | Total recalculado a partir de `Product.price`; preço do client é ignorado. |
| RN12 | Cart por usuário (`OneToOne` em `Cart`); todo endpoint filtra `user=request.auth`. |
| RN13 | Carrinho vazio → 400 com `{"detail": "Cart is empty"}`. |
| RN15 | `Order.payment_method = "default"` (stub, sem cobrança real). |
| RN16 | `POST /orders/{id}/rebuy` exige que o pedido pertença ao `request.auth` (`Order.objects.filter(user=user, pk=order_id)`). Insere itens via `get_or_create` + `F("quantity") + n`, mantendo atomicidade do carrinho. |
| RN17 | `POST /products/{id}/reviews` exige bearer; rating fora de 1–5 → 400; comentário truncado em 1000 chars no servidor (defesa em profundidade contra o `max_length` do model). |

## Django admin

Habilitado em `/admin/` e protegido pelo middleware padrão de auth/session/csrf.
Modelos registrados:

| App | Modelos |
|---|---|
| accounts | `User` (estende `UserAdmin`), `Token` |
| catalog | `Product` (filtros por `type`/`subtype`, busca por `name`/`description`), `Review` (filtro por `rating`, busca por `author`/`comment`/`product__name`) |
| cart | `Cart` (com inline de `CartItem`), `CartItem` |
| orders | `Order` (com inline readonly de `OrderItem`), `OrderItem` |

Estáticos servidos pelo **WhiteNoise** (`whitenoise.middleware.WhiteNoiseMiddleware`):
- Em `DEBUG=1`: `CompressedStaticFilesStorage` (sem manifest) + `whitenoise.runserver_nostatic`,
  então `runserver` e Granian se comportam igual.
- Em `DEBUG=0`: `CompressedManifestStaticFilesStorage` — exige `collectstatic` antes do boot.
  O `Dockerfile` já roda no startup.

### Superusuário padrão

Criado automaticamente pelo comando `seed_admin` (idempotente):

| Campo | Valor |
|---|---|
| username | `admin` |
| email | `admin@admin.local` |
| password | `admin` |

Login via API: `POST /api/auth/login` com `{"email": "admin@admin.local", "password": "admin"}`.
Login via admin: usa o mesmo email/senha em `/admin/`.

> Apenas para desenvolvimento. Trocar antes de qualquer deploy.

Por que o email é `admin@admin.local` e não `admin`? O modelo `User` usa `USERNAME_FIELD = "email"` (login por email na API) e o campo é um `EmailField`, que exige formato válido. `admin@admin.local` satisfaz a validação e mantém a senha curta `admin` para conveniência em dev.

## Catálogo seedado

Definido em `apps/catalog/management/commands/seed_catalog.py`. Idempotente — usa `update_or_create(name=...)` (campo `Product.name` é `unique`). Editar a lista `CATALOG` (+ `_EXTRA_CATALOG`) e rodar `seed_catalog` sincroniza in-place. Total atual: **65 produtos** (15 originais + 50 extras gerados ciclando pelas mesmas 15 URLs Unsplash, todas `image_url` em CDN público) — exibidos no app via Coil, com placeholder em caso de erro de imagem.

O comando também popula **3–8 reviews por produto** (rating, autor e comentário escolhidos por hash determinístico do nome, garantindo seed reproduzível). A criação de reviews é **idempotente por produto**: só insere se `product.reviews` estiver vazio, então rodar `seed_catalog` várias vezes não duplica. Para forçar reseed, apague as reviews (`Review.objects.all().delete()`) antes.

## Fluxo manual via curl

```bash
TOKEN=$(curl -s -X POST localhost:8000/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.com","password":"pw12345!"}' | jq -r .token)

curl -s 'localhost:8000/api/products?q=enem&limit=5' | jq '.items[0], .total'

curl -X POST localhost:8000/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"product_id": 1, "quantity": 2}'

curl -X POST localhost:8000/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

## Autenticação

Endpoints marcados como `bearer` aceitam o token de três formas no header `Authorization`:

```
Authorization: Bearer <token>
Authorization: Token <token>
Authorization: <token>
```

`apps/accounts/auth.BearerAuth` faz `header.split()[-1]` e valida só o último termo, então
qualquer prefixo conhecido (ou nenhum) funciona. Padrão recomendado: `Bearer` (formato OpenAPI
e o que o app Kotlin envia).

## Convenções

- Migrations são commitadas no repo. **Nunca** rodar `makemigrations` no boot do container.
- Seguir Conventional Commits e TDD (red → green → refactor) conforme `CLAUDE.md` da raiz.
- Não commitar nada sem autorização explícita.
