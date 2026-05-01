# Edu — App Android (Kotlin + Jetpack Compose)

Port nativo das telas do app Flutter `estuda_app` para Android usando **Kotlin** e **Jetpack Compose**, mantendo a mesma identidade visual (cores, tipografia Lexend Deca, raios, gradiente de fundo) e o mesmo fluxo de navegação.

> Documentação técnica detalhada (arquitetura, design system, telas, navegação, persistência, integração com back-end) em [`docs/`](docs/README.md).

## Telas implementadas

| Rota | Tela | Origem Flutter |
|---|---|---|
| `login` | Login (autenticado via `POST /api/auth/login`) | `login_screen.dart` |
| `register` | Cadastro (validação local + `POST /api/auth/register`) | `register_screen.dart` |
| `home` | Placeholder pós-login com atalhos | — |
| `marketplace` | EduMarketplace (produtos via `GET /api/products`) | `marketplace_screen.dart` |
| `checkout` | Revisão do Carrinho / Finalizar Pedido | `checkout_screen.dart` |
| `orders` | Seus Pedidos (com stepper de entrega) | `orders_screen.dart` |

## Stack

- Kotlin 2.0.21
- Android Gradle Plugin 8.7.3
- Jetpack Compose BOM 2024.12.01 + Material 3
- Navigation Compose 2.8.5
- Tipografia: Lexend Deca via `androidx.compose.ui:ui-text-google-fonts`
- `minSdk` 24 · `targetSdk`/`compileSdk` 35 · `JavaVersion` 17

## Estrutura

```
edu-kt/
├── build.gradle.kts          # raiz (plugins do projeto)
├── settings.gradle.kts
├── gradle/libs.versions.toml # versions catalog
├── gradle.properties
├── local.properties          # gerado, não commitar (sdk.dir=...)
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/
        │   ├── values/{strings.xml, themes.xml, font_certs.xml}
        │   └── xml/network_security_config.xml   # libera HTTP só p/ IP do backend
        └── java/br/com/edu/
            ├── MainActivity.kt              # NavHost + EduTheme
            ├── core/
            │   ├── auth/TokenStore.kt       # singleton em memória do token
            │   ├── network/{ApiClient, AuthInterceptor}.kt
            │   ├── theme/{Color,Type,Shape,Gradients,Theme}.kt
            │   └── ui/{EduTextField,EduButtons,EduCard,
            │           DottedBorderBox,BottomNavBars}.kt
            └── features/
                ├── auth/
                │   ├── data/AuthRepository.kt
                │   ├── data/remote/{AuthApi, AuthDtos}.kt
                │   └── presentation/{LoginScreen, LoginViewModel,
                │           RegisterScreen, RegisterViewModel}.kt
                └── marketplace/
                    ├── data/MarketplaceRepository.kt
                    ├── data/remote/{ProductApi, ProductDto}.kt
                    ├── domain/Product.kt
                    └── presentation/{MarketplaceScreen, MarketplaceViewModel,
                            CheckoutScreen, OrdersScreen}.kt
```

## Pré-requisitos

| Ferramenta | Versão | Como obter |
|---|---|---|
| JDK | 17 ou 21 (LTS) | Temurin via `asdf install java temurin-21.0.9+10.0.LTS` |
| Android SDK | Platform 35 + Build-Tools 34 | Android Studio ou `cmdline-tools` |
| Docker + Compose | qualquer versão recente | docker.com / pacote da distro |
| Python | 3.10+ (só para o `configure_ip.py`) | já vem na maioria das distros |
| Gradle | não precisa instalar — o wrapper baixa 8.10.2 | — |

Variáveis de ambiente esperadas:

```bash
export JAVA_HOME=$HOME/.asdf/installs/java/temurin-21.0.9+10.0.LTS
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH
```

## Quick start (clonou agora?)

```bash

make doctor   # confere JDK, Docker, porta 8000, AVD e IP atual
make dev      # setup + IP + sobe backend + emulador + instala/abre o app
```

`make dev` faz, em ordem:
1. `make setup` → grava `local.properties` apontando para o Android SDK.
2. `make configure-ip` → detecta o IP da LAN e escreve em `BASE_URL` (`app/build.gradle.kts`)
   **e** no `network_security_config.xml` — sem isso o app falha em silêncio com
   "CLEARTEXT communication not permitted".
3. `make backend-up` → `docker compose up -d --wait`: roda migrações, semeia catálogo
   e admin, sobe o Granian em `:8000` e bloqueia até o healthcheck passar.
4. `make emulator` → sobe o primeiro AVD se ainda não houver device conectado.
5. Aguarda o boot do emulador (`sys.boot_completed=1`) e roda `make run`.

**Login seedado:** `admin@admin.local` / `admin` (criado pelo `seed_admin`).

Em caso de erro de detecção de IP (VPN, segunda interface, etc.), force manualmente:
`make configure-ip IP=192.168.x.x`.

## Atalhos via Makefile

```bash
make help            # lista todos os alvos
make doctor          # diagnóstico (JDK, Docker, porta, AVD, IP)
make dev             # fluxo completo: setup + IP + backend + emulador + app

# IP / config
make configure-ip    # detecta IP e grava em BASE_URL + network_security_config
make configure-ip IP=192.168.0.10     # força um IP específico
make show-ip         # mostra o BASE_URL atual

# Backend (docker compose)
make backend-up      # sobe DB + API com healthcheck (--wait)
make backend-down    # derruba os containers
make backend-restart # reinicia só a API (mantém o DB)
make backend-logs    # logs do container da API
make backend-shell   # shell dentro do container
make seed            # reroda seed_catalog + seed_admin

# Frontend (Android)
make emulator        # sobe AVD em background (AVD=nome para escolher)
make run             # build + install + launch
make logs            # logcat filtrado pelo PID do app
make stop            # force-stop
make clean           # limpa artefatos
```

`make` exporta `JAVA_HOME` (Temurin 21 do asdf) e `ANDROID_HOME` (`~/Android/Sdk`) automaticamente — sobrescreva passando `JAVA_HOME=... make run` se precisar.

## Como subir o app (sem Makefile)

### 1. Build do APK debug

```bash
cd /home/elias/programming/fiap/edu-kt
./gradlew assembleDebug
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

### 2. Instalar em dispositivo / emulador

Com um emulador rodando ou um dispositivo conectado via USB com **depuração USB ativada** (`adb devices` deve listar):

```bash
./gradlew installDebug
```

Para abrir o app já instalado:

```bash
adb shell am start -n br.com.edu/.MainActivity
```

### 3. Subir um emulador (se não tiver dispositivo)

```bash
# listar AVDs disponíveis
$ANDROID_HOME/emulator/emulator -list-avds

# subir um
$ANDROID_HOME/emulator/emulator -avd <nome_do_avd> &
```

Ou criar um novo AVD pelo Android Studio (`Tools → Device Manager`).

### 4. Abrir no Android Studio

`File → Open` e aponte para `/home/elias/programming/fiap/edu-kt`. O Studio sincroniza o Gradle e habilita o botão **Run ▶**.

## Como usar o app

1. **Login** — campos `E-mail` e `Senha`. Use as credenciais de teste:
   - **e-mail:** `teste`
   - **senha:** `teste`
   Qualquer outra combinação exibe Snackbar `Credenciais inválidas. Use teste / teste`. Login bem-sucedido vai para `home`.
2. **Cadastro** — acessível via "Inscreva-se no Edu" ou pela barra inferior. Validações:
   - Todos os campos obrigatórios.
   - E-mail precisa conter `@`.
   - Senha ≥ 8 caracteres **e** ao menos 1 caractere especial (`!@#$%^&*(),.?":{}|<>`).
   - Confirmação igual à senha.
3. **Home** — placeholder com botões "Abrir Marketplace" e "Seus Pedidos".
4. **Marketplace** — ícone do carrinho na top bar abre o **Checkout**; ícone de notificação abre **Seus Pedidos**.
5. **Checkout** — alternar entre "Cartão de Crédito" e "PIX" move a borda roxa e o check-badge.
6. **Pedidos** — o stepper destaca a etapa atual em roxo (no mock, "Trânsito").

## Comandos úteis

```bash
# Build limpo
./gradlew clean assembleDebug

# Apenas verificar compilação
./gradlew compileDebugKotlin

# Build de release (sem assinatura configurada — apenas para checagem)
./gradlew assembleRelease

# Rodar lint
./gradlew lintDebug

# Listar tarefas
./gradlew tasks

# Desinstalar
adb uninstall br.com.edu
```

## Troubleshooting

- **`SDK location not found`** — crie/edite `local.properties` com `sdk.dir=/caminho/para/Android/Sdk`.
- **`Unsupported class file major version`** — o JDK em uso é mais antigo que 17. Aponte `JAVA_HOME` para um JDK 17 ou 21.
- **Build trava em `:app:processDebugMainManifest` ou similar** — rode `./gradlew --stop` e tente novamente; se persistir, `rm -rf ~/.gradle/caches/build-cache-1` e rebuilde.
- **Fontes Lexend Deca não aparecem** — o `ui-text-google-fonts` baixa a fonte na primeira execução (precisa de internet no emulador/dispositivo). Sem rede, o sistema usa fallback.
- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`** — desinstale a versão antiga: `adb uninstall br.com.edu`.
- **App não conecta no backend / `Connection refused` ou `CLEARTEXT not permitted`** — o IP da máquina mudou. Rode `make configure-ip` (ou `make configure-ip IP=192.168.x.x` se a detecção pegar o IP errado — VPN, segunda interface) e rebuilde com `make run`. Confira com `make show-ip`. O script já atualiza o `network_security_config.xml`.
- **`make backend-up` trava em "waiting for healthy"** — `make backend-logs` para ver o erro. Causas comuns: porta 8000 ocupada (`make doctor` avisa); migration falhou (apaguar `back-end/data/db.sqlite3` e tentar de novo).
- **Login falha com `Invalid credentials`** — o seed do admin pode não ter rodado. `make seed` reaplica.

## Identidade visual

Tokens reproduzidos 1:1 do Flutter — ver `app/src/main/java/br/com/edu/core/theme/Color.kt`. Principais:

- Roxo `#5B00DF` (acentos, links, seleção)
- Primary `#1A1A2E` (textos e botões principais)
- Gradiente de fundo `#A9CADD → #BDD5E5 → #D1E0EE`
- Fill de input `#F3F4F6`
- Sucesso `#22C55E` · Erro `#DC2626`
- Variantes suaves: `PurpleSoft #EDE0FF`, `GreenSoft #D1F4DD`
