# Edu Backend — Marketplace Orders API

Django + Django Ninja + SQLite. Serve JSON para o app Kotlin.

## Stack

- Python 3.12 · Django 6 · django-ninja
- SQLite (dev/local)
- Granian (ASGI server) com hot reload
- Pydantic via Ninja Schemas
- uv para gerenciamento de pacotes

## Setup local

```bash
uv sync
uv run python manage.py migrate
uv run python manage.py seed_catalog
uv run python manage.py seed_admin
uv run granian --interface asgi config.asgi:application --host 0.0.0.0 --port 8000 --reload
```

API em `http://localhost:8000/api/` · Docs OpenAPI em `http://localhost:8000/api/docs`.

## Docker

```bash
docker compose up --build
```

Dois serviços:

- **`migrate`** — one-shot. Roda `manage.py migrate` + `seed_catalog` + `seed_admin` e termina.
- **`api`** — sobe Granian com `--reload`. Depende de `migrate` finalizar com sucesso (`service_completed_successfully`).

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
│   ├── catalog/                # Product + GET /products
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
| GET | `/api/products` | — | Lista catálogo (`name, type, subtype, description, price`) |
| GET | `/api/cart` | bearer | Itens (`product_id, name, type, subtype, price, quantity, subtotal`) + total |
| POST | `/api/cart/items` | bearer | `{product_id, quantity}` — incrementa se já existir |
| DELETE | `/api/cart/items/{product_id}?quantity=N` | bearer | Remove item; com `quantity` decrementa (remove se ≥ atual); sem `quantity` apaga o item inteiro |
| POST | `/api/orders` | bearer | Cria pedido a partir do carrinho e o esvazia |
| GET | `/api/orders` | bearer | Histórico de pedidos do usuário |

## Regras de negócio implementadas

| RN | Garantia |
|---|---|
| RN01 | `GET /products` retorna `name, type, subtype, description, price`. |
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

## Superusuário padrão

Criado automaticamente pelo comando `seed_admin` (idempotente):

| Campo | Valor |
|---|---|
| username | `admin` |
| email | `admin@admin.local` |
| password | `admin` |

Login via API: `POST /api/auth/login` com `{"email": "admin@admin.local", "password": "admin"}`.

> Apenas para desenvolvimento. Trocar antes de qualquer deploy.

Por que o email é `admin@admin.local` e não `admin`? O modelo `User` usa `USERNAME_FIELD = "email"` (login por email na API) e o campo é um `EmailField`, que exige formato válido. `admin@admin.local` satisfaz a validação e mantém a senha curta `admin` para conveniência em dev.

## Catálogo seedado

Definido em `apps/catalog/management/commands/seed_catalog.py`. Idempotente — usa `update_or_create(name=...)` (campo `Product.name` é `unique`). Editar a lista `CATALOG` e rodar `seed_catalog` sincroniza in-place.

## Fluxo manual via curl

```bash
TOKEN=$(curl -s -X POST localhost:8000/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.com","password":"pw12345!"}' | jq -r .token)

curl -s localhost:8000/api/products | jq '.[0]'

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
