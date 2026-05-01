# Edu — Makefile
# Atalhos para build, instalação e desenvolvimento do app Android + backend.
# Uso: `make <alvo>` — execute `make help` para listar todos.

# ---- Configuração ----------------------------------------------------------
JAVA_HOME    ?= $(HOME)/.asdf/installs/java/temurin-21.0.9+10.0.LTS
ANDROID_HOME ?= $(HOME)/Android/Sdk
APP_ID       := br.com.edu
ACTIVITY     := $(APP_ID)/.MainActivity
APK          := app/build/outputs/apk/debug/app-debug.apk
GRADLE       := ./gradlew
ADB          := $(ANDROID_HOME)/platform-tools/adb
EMULATOR     := $(ANDROID_HOME)/emulator/emulator
COMPOSE      := docker compose -f back-end/docker-compose.yml

export JAVA_HOME
export ANDROID_HOME
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/platform-tools:$(PATH)

.DEFAULT_GOAL := help
.PHONY: help setup configure-ip show-ip doctor dev \
        build compile release clean lint test install launch run stop uninstall logs \
        devices avds emulator wrapper \
        backend-up backend-down backend-restart backend-logs backend-shell seed \
        backend-test backend-lint backend-format

# ---- Ajuda -----------------------------------------------------------------
help: ## Lista todos os alvos
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ---- Setup -----------------------------------------------------------------
setup: ## Cria local.properties apontando para o Android SDK
	@echo "sdk.dir=$(ANDROID_HOME)" > local.properties
	@echo "→ local.properties gravado (sdk.dir=$(ANDROID_HOME))"

configure-ip: ## Detecta o IP da LAN e grava em BASE_URL + network_security_config (use IP=… p/ forçar, PORT=… p/ porta)
	@python3 scripts/configure_ip.py $(if $(IP),--ip $(IP)) $(if $(PORT),--port $(PORT))

show-ip: ## Mostra o BASE_URL atual configurado no app
	@python3 scripts/configure_ip.py --show

doctor: ## Verifica JDK, Android SDK, Docker, porta 8000 livre, AVD e IP do app
	@echo "JAVA_HOME    = $(JAVA_HOME)"
	@echo "ANDROID_HOME = $(ANDROID_HOME)"
	@$(JAVA_HOME)/bin/java -version 2>&1 | head -1 | sed 's/^/  /' || echo "  ✗ JDK não encontrado em JAVA_HOME"
	@command -v docker >/dev/null && docker info >/dev/null 2>&1 && echo "  ✓ Docker rodando" || echo "  ✗ Docker não está rodando"
	@(ss -ltn 2>/dev/null || netstat -ltn 2>/dev/null) | grep -q ':8000 ' \
		&& echo "  ⚠ Porta 8000 já em uso (container do backend ou outro processo?)" \
		|| echo "  ✓ Porta 8000 livre"
	@AVD=$$($(EMULATOR) -list-avds 2>/dev/null | head -1); \
		[ -n "$$AVD" ] && echo "  ✓ AVD disponível: $$AVD" || echo "  ✗ Nenhum AVD criado (use Android Studio para criar um)"
	@echo "--- adb devices ---"
	@$(ADB) devices
	@$(MAKE) -s show-ip

# ---- Fluxo completo --------------------------------------------------------
dev: setup configure-ip backend-up emulator ## Setup + IP + sobe backend + emulador + roda app (fluxo completo)
	@echo "→ aguardando emulador ficar pronto..."
	@$(ADB) wait-for-device
	@until [ "$$($(ADB) shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
	@echo "  ✓ emulador pronto"
	@$(MAKE) -s run
	@echo ""
	@echo "→ App rodando. Login seedado: admin@admin.local / admin"
	@echo "  Logs do app:     make logs"
	@echo "  Logs do backend: make backend-logs"

# ---- Build -----------------------------------------------------------------
build: ## Compila o APK debug
	$(GRADLE) assembleDebug

compile: ## Apenas compila o Kotlin (rápido, sem empacotar)
	$(GRADLE) compileDebugKotlin

release: ## Build de release (sem assinatura configurada)
	$(GRADLE) assembleRelease

clean: ## Limpa artefatos de build
	$(GRADLE) clean

lint: ## Roda Android Lint no debug
	$(GRADLE) lintDebug

test: ## Roda os testes unitários do app (JVM)
	$(GRADLE) testDebugUnitTest

# ---- Instalação e execução -------------------------------------------------
install: ## Instala o APK debug no dispositivo/emulador conectado
	$(GRADLE) installDebug

launch: ## Abre o app já instalado
	$(ADB) shell am start -n $(ACTIVITY)

run: install launch ## Build + install + launch (fluxo padrão de dev)

stop: ## Força a parada do app
	$(ADB) shell am force-stop $(APP_ID)

uninstall: ## Desinstala o app
	$(ADB) uninstall $(APP_ID) || true

logs: ## Streamia logs do app via logcat (Ctrl+C para sair)
	$(ADB) logcat --pid=$$($(ADB) shell pidof -s $(APP_ID))

# ---- Backend (docker compose) ---------------------------------------------
backend-up: ## Sobe o backend (migra, semeia, sobe API) e espera ficar healthy
	$(COMPOSE) up -d --wait
	@echo "  ✓ backend healthy em http://localhost:8000/api/"

backend-down: ## Derruba os containers do backend
	$(COMPOSE) down

backend-restart: ## Reinicia só o serviço da API (mantém o DB)
	$(COMPOSE) restart api

backend-logs: ## Logs do container da API (Ctrl+C para sair)
	$(COMPOSE) logs -f api

backend-shell: ## Abre shell no container da API
	$(COMPOSE) exec api sh

seed: ## Reroda os management commands de seed (catálogo + admin)
	$(COMPOSE) exec api uv run python manage.py seed_catalog
	$(COMPOSE) exec api uv run python manage.py seed_admin

backend-test: ## Roda pytest dentro do container da API (usa back-end/conftest.py)
	$(COMPOSE) exec api uv run pytest

backend-lint: ## Roda ruff check no código do backend
	$(COMPOSE) exec api uv run ruff check .

backend-format: ## Aplica ruff format no código do backend
	$(COMPOSE) exec api uv run ruff format .

# ---- Emulador / dispositivos ----------------------------------------------
devices: ## Lista dispositivos/emuladores conectados
	$(ADB) devices

avds: ## Lista AVDs (emuladores) disponíveis
	$(EMULATOR) -list-avds

emulator: ## Sobe o primeiro AVD em background se não houver device (use AVD=nome para escolher)
	@if $(ADB) devices | grep -qE 'emulator-|device$$' | grep -v 'List of'; then \
		echo "  ✓ device/emulador já conectado"; \
	else \
		AVD="$${AVD:-$$($(EMULATOR) -list-avds | head -1)}"; \
		if [ -z "$$AVD" ]; then echo "✗ Nenhum AVD encontrado. Crie um pelo Android Studio."; exit 1; fi; \
		echo "→ subindo emulador: $$AVD"; \
		nohup $(EMULATOR) -avd $$AVD > /tmp/edu-emulator.log 2>&1 & \
		echo "  PID $$! · log: /tmp/edu-emulator.log"; \
	fi

# ---- Misc ------------------------------------------------------------------
wrapper: ## Regenera o Gradle wrapper (8.10.2)
	gradle wrapper --gradle-version 8.10.2 --distribution-type bin
