FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    UV_LINK_MODE=copy \
    UV_PROJECT_ENVIRONMENT=/app/.venv

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && curl -LsSf https://astral.sh/uv/install.sh | sh \
    && mv /root/.local/bin/uv /usr/local/bin/uv

COPY pyproject.toml uv.lock* ./
RUN uv sync --frozen --no-dev || uv sync --no-dev

COPY . .

RUN mkdir -p /data
ENV DJANGO_DB_PATH=/data/db.sqlite3

EXPOSE 8000

CMD sh -c "uv run python manage.py migrate --noinput \
    && uv run python manage.py loaddata apps/catalog/fixtures/seed.json \
    && uv run granian --interface asgi config.asgi:application --host 0.0.0.0 --port 8000"
