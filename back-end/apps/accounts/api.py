from datetime import date

from django.contrib.auth import authenticate
from django.db import IntegrityError, transaction
from ninja import Router, Status
from ninja_jwt.exceptions import TokenError
from ninja_jwt.tokens import RefreshToken

from apps.accounts.auth import jwt_auth
from apps.accounts.models import Address, User
from apps.accounts.schemas import (
    AddressIn,
    AddressOut,
    AddressPatchIn,
    LoginIn,
    LogoutIn,
    RefreshIn,
    RefreshOut,
    RegisterIn,
    TokenPairOut,
    UserOut,
    UserPatchIn,
)

router = Router()


def _split_name(full_name: str) -> tuple[str, str]:
    full_name = (full_name or "").strip()
    if not full_name:
        return "", ""
    parts = full_name.split(" ", 1)
    first = parts[0][:150]
    last = (parts[1] if len(parts) > 1 else "")[:150]
    return first, last


def _parse_birth_date(value: str) -> date | None:
    value = (value or "").strip()
    if not value:
        return None
    try:
        return date.fromisoformat(value)
    except ValueError as exc:
        raise ValueError("Invalid birth_date format, expected YYYY-MM-DD") from exc


def _serialize_user(user: User) -> dict:
    full_name = f"{user.first_name} {user.last_name}".strip()
    return {
        "id": user.id,
        "email": user.email,
        "name": full_name,
        "phone": user.phone or "",
        "birth_date": user.birth_date.isoformat() if user.birth_date else None,
        "date_joined": user.date_joined.isoformat(),
    }


def _issue_pair(user: User) -> dict:
    refresh = RefreshToken.for_user(user)
    return {
        "access": str(refresh.access_token),
        "refresh": str(refresh),
        "email": user.email,
    }


@router.post("/register", response={201: TokenPairOut, 400: dict})
def register(request, payload: RegisterIn):
    try:
        birth_date = _parse_birth_date(payload.birth_date)
    except ValueError as exc:
        return Status(400, {"detail": str(exc)})

    first, last = _split_name(payload.name)
    try:
        user = User.objects.create_user(
            username=payload.email,
            email=payload.email,
            password=payload.password,
            first_name=first,
            last_name=last,
        )
    except IntegrityError:
        return Status(400, {"detail": "Email already registered"})

    user.phone = (payload.phone or "")[:32]
    user.birth_date = birth_date
    user.save(update_fields=["phone", "birth_date"])

    return Status(201, _issue_pair(user))


@router.post("/login", response={200: TokenPairOut, 401: dict})
def login(request, payload: LoginIn):
    user = authenticate(request, username=payload.email, password=payload.password)
    if user is None:
        return Status(401, {"detail": "Invalid credentials"})
    return Status(200, _issue_pair(user))


@router.post("/refresh", response={200: RefreshOut, 401: dict})
def refresh(request, payload: RefreshIn):
    try:
        old = RefreshToken(payload.refresh)
    except TokenError as exc:
        return Status(401, {"detail": str(exc)})

    user_id = old.get("user_id")
    try:
        user = User.objects.get(pk=user_id, is_active=True)
    except User.DoesNotExist:
        return Status(401, {"detail": "User not found"})

    old.blacklist()
    new_pair = RefreshToken.for_user(user)
    return Status(200, {"access": str(new_pair.access_token), "refresh": str(new_pair)})


@router.post("/logout", auth=jwt_auth, response={204: None, 401: dict})
def logout(request, payload: LogoutIn):
    try:
        RefreshToken(payload.refresh).blacklist()
    except TokenError as exc:
        return Status(401, {"detail": str(exc)})
    return Status(204, None)


@router.get("/me", auth=jwt_auth, response=UserOut)
def me(request):
    return _serialize_user(request.auth)


@router.patch("/me", auth=jwt_auth, response={200: UserOut, 400: dict})
def update_me(request, payload: UserPatchIn):
    user: User = request.auth
    update_fields: list[str] = []

    if payload.name is not None:
        first, last = _split_name(payload.name)
        user.first_name = first
        user.last_name = last
        update_fields += ["first_name", "last_name"]

    if payload.phone is not None:
        user.phone = payload.phone[:32]
        update_fields.append("phone")

    if payload.birth_date is not None:
        try:
            user.birth_date = _parse_birth_date(payload.birth_date)
        except ValueError as exc:
            return Status(400, {"detail": str(exc)})
        update_fields.append("birth_date")

    if update_fields:
        user.save(update_fields=update_fields)

    return Status(200, _serialize_user(user))


def _serialize_address(address: Address) -> dict:
    return {
        "id": address.id,
        "label": address.label or "",
        "zip_code": address.zip_code,
        "street": address.street,
        "number": address.number,
        "complement": address.complement or "",
        "neighborhood": address.neighborhood,
        "city": address.city,
        "state": address.state,
        "is_favorite": address.is_favorite,
    }


def _clear_other_favorites(user: User, exclude_id: int | None = None) -> None:
    qs = Address.objects.select_for_update().filter(user=user, is_favorite=True)
    if exclude_id is not None:
        qs = qs.exclude(id=exclude_id)
    qs.update(is_favorite=False)


@router.get("/addresses", auth=jwt_auth, response=list[AddressOut])
def list_addresses(request):
    user: User = request.auth
    return [_serialize_address(a) for a in user.addresses.all()]


@router.post("/addresses", auth=jwt_auth, response={201: AddressOut})
def create_address(request, payload: AddressIn):
    user: User = request.auth
    with transaction.atomic():
        if payload.is_favorite:
            _clear_other_favorites(user)
        address = Address.objects.create(user=user, **payload.dict())
    return Status(201, _serialize_address(address))


@router.patch("/addresses/{address_id}", auth=jwt_auth, response={200: AddressOut, 404: dict})
def update_address(request, address_id: int, payload: AddressPatchIn):
    user: User = request.auth
    with transaction.atomic():
        try:
            address = Address.objects.select_for_update().get(id=address_id, user=user)
        except Address.DoesNotExist:
            return Status(404, {"detail": "Address not found"})

        data = payload.dict(exclude_unset=True)
        if data.get("is_favorite") is True:
            _clear_other_favorites(user, exclude_id=address.id)

        for field, value in data.items():
            setattr(address, field, value)
        address.save()

    return Status(200, _serialize_address(address))


@router.delete("/addresses/{address_id}", auth=jwt_auth, response={204: None, 404: dict})
def delete_address(request, address_id: int):
    user: User = request.auth
    deleted, _ = Address.objects.filter(id=address_id, user=user).delete()
    if not deleted:
        return Status(404, {"detail": "Address not found"})
    return Status(204, None)
