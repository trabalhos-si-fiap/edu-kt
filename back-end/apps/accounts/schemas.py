from ninja import Schema


class RegisterIn(Schema):
    email: str
    password: str
    name: str = ""
    phone: str = ""
    birth_date: str = ""


class LoginIn(Schema):
    email: str
    password: str


class TokenOut(Schema):
    token: str
    email: str
