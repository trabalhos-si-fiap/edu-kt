import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.getenv("DJANGO_SECRET_KEY", "dev-insecure-change-me")
DEBUG = os.getenv("DJANGO_DEBUG", "1") == "1"
ALLOWED_HOSTS = ["*"]

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "whitenoise.runserver_nostatic",
    "django.contrib.staticfiles",
    "corsheaders",
    "apps.accounts",
    "apps.catalog",
    "apps.cart",
    "apps.orders",
    "apps.support",
]

MIDDLEWARE = [
    "corsheaders.middleware.CorsMiddleware",
    "django.middleware.security.SecurityMiddleware",
    "whitenoise.middleware.WhiteNoiseMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
]

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

STATIC_URL = "static/"
STATIC_ROOT = BASE_DIR / "staticfiles"
STORAGES = {
    "default": {"BACKEND": "django.core.files.storage.FileSystemStorage"},
    "staticfiles": {
        "BACKEND": (
            "whitenoise.storage.CompressedManifestStaticFilesStorage"
            if not DEBUG
            else "whitenoise.storage.CompressedStaticFilesStorage"
        ),
    },
}

ROOT_URLCONF = "config.urls"
WSGI_APPLICATION = "config.wsgi.application"
ASGI_APPLICATION = "config.asgi.application"

DB_PATH = os.getenv("DJANGO_DB_PATH", str(BASE_DIR / "data" / "db.sqlite3"))
Path(DB_PATH).parent.mkdir(parents=True, exist_ok=True)

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.sqlite3",
        "NAME": DB_PATH,
    }
}

AUTH_USER_MODEL = "accounts.User"
DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"
USE_TZ = True
TIME_ZONE = "UTC"
LANGUAGE_CODE = "en-us"

CORS_ALLOW_ALL_ORIGINS = True
