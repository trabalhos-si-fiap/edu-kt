from ninja import Router

from apps.catalog.models import Product
from apps.catalog.schemas import ProductOut

router = Router()


@router.get("", response=list[ProductOut])
def list_products(request):
    return list(Product.objects.all())
