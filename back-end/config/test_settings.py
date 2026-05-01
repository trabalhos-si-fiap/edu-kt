"""
Settings module used exclusively by the pytest suite.

Sets a deterministic SECRET_KEY before delegating to `config.settings`, so the
test run never depends on a developer's `.env` or shell environment and uses
the exact same code path as production for everything else.
"""

import os

os.environ.setdefault(
    "DJANGO_SECRET_KEY",
    "pytest-secret-key-deterministic-and-only-used-during-tests",
)

from config.settings import *  # noqa: E402, F401, F403
