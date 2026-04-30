from ninja import NinjaAPI

from apps.accounts.api import router as accounts_router
from apps.cart.api import router as cart_router
from apps.catalog.api import router as catalog_router
from apps.orders.api import router as orders_router

api = NinjaAPI(title="Edu Marketplace API", version="1.0.0")
api.add_router("/auth", accounts_router)
api.add_router("/products", catalog_router)
api.add_router("/cart", cart_router)
api.add_router("/orders", orders_router)
