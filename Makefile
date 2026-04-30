# Edu — Makefile
# Atalhos para build, instalação e desenvolvimento do app Android.
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

export JAVA_HOME
export ANDROID_HOME
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/platform-tools:$(PATH)

.DEFAULT_GOAL := help
.PHONY: help setup build install run launch stop logs uninstall clean lint \
        compile release avds emulator devices wrapper doctor

# ---- Ajuda -----------------------------------------------------------------
help: ## Lista todos os alvos
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ---- Setup -----------------------------------------------------------------
setup: ## Cria local.properties apontando para o Android SDK
	@echo "sdk.dir=$(ANDROID_HOME)" > local.properties
	@echo "→ local.properties gravado (sdk.dir=$(ANDROID_HOME))"

doctor: ## Verifica JDK, Android SDK e dispositivos conectados
	@echo "JAVA_HOME    = $(JAVA_HOME)"
	@echo "ANDROID_HOME = $(ANDROID_HOME)"
	@$(JAVA_HOME)/bin/java -version
	@echo "--- adb devices ---"
	@$(ADB) devices

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

# ---- Emulador / dispositivos ----------------------------------------------
devices: ## Lista dispositivos/emuladores conectados
	$(ADB) devices

avds: ## Lista AVDs (emuladores) disponíveis
	$(EMULATOR) -list-avds

emulator: ## Sobe o primeiro AVD encontrado em background (use AVD=nome para escolher)
	@AVD="$${AVD:-$$($(EMULATOR) -list-avds | head -1)}"; \
	if [ -z "$$AVD" ]; then echo "✗ Nenhum AVD encontrado. Crie um pelo Android Studio."; exit 1; fi; \
	echo "→ subindo emulador: $$AVD"; \
	nohup $(EMULATOR) -avd $$AVD > /tmp/edu-emulator.log 2>&1 & \
	echo "  PID $$! · log: /tmp/edu-emulator.log"

# ---- Misc ------------------------------------------------------------------
wrapper: ## Regenera o Gradle wrapper (8.10.2)
	gradle wrapper --gradle-version 8.10.2 --distribution-type bin
