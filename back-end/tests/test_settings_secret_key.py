"""
Tests for the SECRET_KEY fallback in config.settings.

The settings module is imported once at process startup, so to exercise the
fallback branches we re-import it under a manipulated environment using
importlib.reload + monkeypatched env vars.
"""

import importlib
import logging
import os

import pytest
from django.core.exceptions import ImproperlyConfigured


def _reload_settings():
    import config.settings as settings_module

    return importlib.reload(settings_module)


@pytest.fixture(autouse=True)
def _restore_settings():
    """Reload settings back to the test environment after each test."""
    original_env = {
        "DJANGO_SECRET_KEY": os.environ.get("DJANGO_SECRET_KEY"),
        "DJANGO_DEBUG": os.environ.get("DJANGO_DEBUG"),
    }
    yield
    for key, value in original_env.items():
        if value is None:
            os.environ.pop(key, None)
        else:
            os.environ[key] = value
    try:
        _reload_settings()
    except ImproperlyConfigured:
        # Test environment must always have a usable SECRET_KEY; if it does not,
        # fall back to a known dev value so subsequent tests can import settings.
        os.environ.setdefault("DJANGO_SECRET_KEY", "test-restore-key-32-bytes-minimum-aaa")
        _reload_settings()


def test_uses_env_secret_key_when_set(monkeypatch, caplog):
    monkeypatch.setenv("DJANGO_SECRET_KEY", "from-env-secret")
    monkeypatch.setenv("DJANGO_DEBUG", "1")
    with caplog.at_level(logging.WARNING, logger="config.settings"):
        settings = _reload_settings()
    assert settings.SECRET_KEY == "from-env-secret"
    assert not any("INSECURE" in rec.getMessage() for rec in caplog.records)


def test_falls_back_to_insecure_key_in_debug(monkeypatch, caplog):
    monkeypatch.delenv("DJANGO_SECRET_KEY", raising=False)
    monkeypatch.setenv("DJANGO_DEBUG", "1")
    with caplog.at_level(logging.WARNING, logger="config.settings"):
        settings = _reload_settings()
    assert settings.SECRET_KEY == "INSECURE-askldiqj"
    assert settings.INSECURE_DEV_SECRET_KEY == "INSECURE-askldiqj"
    assert any(
        "INSECURE" in rec.getMessage() and rec.levelno == logging.WARNING for rec in caplog.records
    ), "expected an INSECURE-key warning log when falling back"


def test_raises_in_production_without_secret_key(monkeypatch):
    monkeypatch.delenv("DJANGO_SECRET_KEY", raising=False)
    monkeypatch.setenv("DJANGO_DEBUG", "0")
    with pytest.raises(ImproperlyConfigured):
        _reload_settings()
