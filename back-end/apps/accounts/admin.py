from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as DjangoUserAdmin

from apps.accounts.models import Token, User


@admin.register(User)
class UserAdmin(DjangoUserAdmin):
    list_display = ("id", "email", "username", "is_staff", "is_active", "date_joined")
    list_filter = ("is_staff", "is_active", "is_superuser")
    search_fields = ("email", "username")
    ordering = ("id",)


@admin.register(Token)
class TokenAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "key", "created_at")
    search_fields = ("user__email", "user__username", "key")
    readonly_fields = ("key", "created_at")
    ordering = ("-created_at",)
