from django.core.validators import MaxValueValidator, MinValueValidator
from django.db import models


class Product(models.Model):
    name = models.CharField(max_length=200, unique=True)
    type = models.CharField(max_length=80)
    subtype = models.CharField(max_length=80, blank=True)
    description = models.TextField(blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    image_url = models.URLField(max_length=500, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["id"]


class Review(models.Model):
    product = models.ForeignKey(
        Product, on_delete=models.CASCADE, related_name="reviews"
    )
    author = models.CharField(max_length=120)
    rating = models.PositiveSmallIntegerField(
        validators=[MinValueValidator(1), MaxValueValidator(5)]
    )
    comment = models.TextField(max_length=1000, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-created_at", "-id"]
        indexes = [models.Index(fields=["product", "-created_at"])]
