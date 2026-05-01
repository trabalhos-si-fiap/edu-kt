from ninja import Schema
from pydantic import Field


class RegisterIn(Schema):
    email: str
    password: str
    name: str = ""
    phone: str = ""
    birth_date: str = ""


class LoginIn(Schema):
    email: str
    password: str


class TokenPairOut(Schema):
    access: str
    refresh: str
    email: str


class RefreshIn(Schema):
    refresh: str


class RefreshOut(Schema):
    access: str
    refresh: str


class LogoutIn(Schema):
    refresh: str


class UserOut(Schema):
    id: int
    email: str
    name: str
    phone: str
    birth_date: str | None
    date_joined: str


class UserPatchIn(Schema):
    name: str | None = Field(default=None, max_length=150)
    phone: str | None = Field(default=None, max_length=32)
    birth_date: str | None = None


class AddressIn(Schema):
    label: str = Field(default="", max_length=40)
    zip_code: str = Field(max_length=9)
    street: str = Field(max_length=120)
    number: str = Field(max_length=20)
    complement: str = Field(default="", max_length=80)
    neighborhood: str = Field(max_length=80)
    city: str = Field(max_length=80)
    state: str = Field(max_length=2)
    is_favorite: bool = False


class AddressPatchIn(Schema):
    label: str | None = Field(default=None, max_length=40)
    zip_code: str | None = Field(default=None, max_length=9)
    street: str | None = Field(default=None, max_length=120)
    number: str | None = Field(default=None, max_length=20)
    complement: str | None = Field(default=None, max_length=80)
    neighborhood: str | None = Field(default=None, max_length=80)
    city: str | None = Field(default=None, max_length=80)
    state: str | None = Field(default=None, max_length=2)
    is_favorite: bool | None = None


class AddressOut(Schema):
    id: int
    label: str
    zip_code: str
    street: str
    number: str
    complement: str
    neighborhood: str
    city: str
    state: str
    is_favorite: bool
