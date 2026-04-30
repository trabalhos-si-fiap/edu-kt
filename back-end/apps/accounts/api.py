from django.contrib.auth import authenticate
from django.db import IntegrityError
from ninja import Router

from apps.accounts.models import Token, User
from apps.accounts.schemas import LoginIn, RegisterIn, TokenOut

router = Router()


@router.post("/register", response={201: TokenOut, 400: dict})
def register(request, payload: RegisterIn):
    try:
        user = User.objects.create_user(
            username=payload.email,
            email=payload.email,
            password=payload.password,
            first_name=payload.name[:150],
        )
    except IntegrityError:
        return 400, {"detail": "Email already registered"}
    token = Token.objects.create(user=user)
    return 201, {"token": str(token.key), "email": user.email}


@router.post("/login", response={200: TokenOut, 401: dict})
def login(request, payload: LoginIn):
    user = authenticate(request, username=payload.email, password=payload.password)
    if user is None:
        return 401, {"detail": "Invalid credentials"}
    token = Token.objects.create(user=user)
    return 200, {"token": str(token.key), "email": user.email}
