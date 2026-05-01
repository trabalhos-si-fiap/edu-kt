from django.contrib import admin

from apps.orders.models import Order, OrderItem


class OrderItemInline(admin.TabularInline):
    model = OrderItem
    extra = 0
    autocomplete_fields = ("product",)
    readonly_fields = ("product_name", "unit_price", "quantity")
    can_delete = False


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "total", "payment_method", "created_at")
    list_filter = ("payment_method", "created_at")
    search_fields = ("user__email", "user__username")
    readonly_fields = ("created_at",)
    inlines = [OrderItemInline]
    ordering = ("-created_at",)


@admin.register(OrderItem)
class OrderItemAdmin(admin.ModelAdmin):
    list_display = ("id", "order", "product", "product_name", "unit_price", "quantity")
    search_fields = ("order__user__email", "product_name")
    autocomplete_fields = ("order", "product")
