from django.contrib import admin

from apps.support.models import SupportMessage


@admin.register(SupportMessage)
class SupportMessageAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "sender", "created_at")
    list_filter = ("sender", "created_at")
    search_fields = ("user__email", "user__username", "body")
    readonly_fields = ("created_at",)
    ordering = ("-created_at",)
