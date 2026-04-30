from django.db import models


class Product(models.Model):
    name = models.CharField(max_length=200, unique=True)
    type = models.CharField(max_length=80)
    subtype = models.CharField(max_length=80, blank=True)
    description = models.TextField(blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["id"]
