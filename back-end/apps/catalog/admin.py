from django.contrib import admin

from apps.catalog.models import Product, Review


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ("id", "name", "type", "subtype", "price", "created_at")
    list_filter = ("type", "subtype")
    search_fields = ("name", "description")
    ordering = ("id",)


@admin.register(Review)
class ReviewAdmin(admin.ModelAdmin):
    list_display = ("id", "product", "author", "rating", "created_at")
    list_filter = ("rating",)
    search_fields = ("author", "comment", "product__name")
    ordering = ("-created_at",)
