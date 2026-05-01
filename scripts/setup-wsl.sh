#!/usr/bin/env bash
# Setup interativo do lado WSL/Ubuntu para o projeto Edu.
#
# Idempotente: pode ser rodado várias vezes. Etapas já concluídas são puladas.
#
# Uso:
#   ~/setup-wsl.sh                              # detecta seu usuário Windows automaticamente
#   ~/setup-wsl.sh --windows-user <usuario>     # força um usuário Windows específico
#   ~/setup-wsl.sh --repo-url <git-url>         # clona um fork específico
#   ~/setup-wsl.sh --no-clone                   # pula clone (usa repo já existente em ~/edu-kt)

set -euo pipefail

# ------- helpers ------------------------------------------------------------
c_cyan='\033[1;36m'; c_green='\033[1;32m'; c_yellow='\033[1;33m'; c_red='\033[1;31m'; c_gray='\033[0;90m'; c_reset='\033[0m'
step() { echo -e "\n${c_cyan}=== $* ===${c_reset}"; }
ok()   { echo -e "  ${c_green}[ok]${c_reset} $*"; }
skip() { echo -e "  ${c_gray}[skip]${c_reset} $*"; }
warn() { echo -e "  ${c_yellow}[warn]${c_reset} $*"; }
err()  { echo -e "  ${c_red}[err]${c_reset} $*"; }

confirm() {
    local q="$1" def="${2:-Y}" suffix
    [[ "$def" == "Y" ]] && suffix="[Y/n]" || suffix="[y/N]"
    while true; do
        read -r -p "$q $suffix " ans || ans=""
        ans="${ans:-$def}"
        case "${ans,,}" in
            y|yes|s|sim) return 0 ;;
            n|no|nao|não) return 1 ;;
            *) warn "Responda y ou n." ;;
        esac
    done
}

# ------- args ---------------------------------------------------------------
WIN_USER=""
REPO_URL=""
DO_CLONE=1
REPO_DIR="$HOME/edu-kt"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --windows-user) WIN_USER="$2"; shift 2 ;;
        --repo-url)     REPO_URL="$2"; shift 2 ;;
        --no-clone)     DO_CLONE=0;    shift ;;
        --repo-dir)     REPO_DIR="$2"; shift 2 ;;
        -h|--help)
            sed -n '2,12p' "$0"
            exit 0
            ;;
        *) err "Argumento desconhecido: $1"; exit 1 ;;
    esac
done

# ------- sanidade -----------------------------------------------------------
if ! grep -qi microsoft /proc/version 2>/dev/null; then
    warn "Não estou rodando dentro do WSL. Continue por sua conta e risco."
fi

echo -e "${c_cyan}
  Edu - Setup do lado WSL
  -------------------------
  Vai instalar deps no Ubuntu, apontar o Android SDK do Windows e
  preparar o repo para 'make dev'.
${c_reset}"

# 1. apt deps ---------------------------------------------------------------
step "1/5  Pacotes apt (make, python3, openjdk-21-jdk, git, curl, unzip)"

NEED_PKGS=()
for pkg in make python3 python3-pip openjdk-21-jdk git curl unzip; do
    if dpkg -s "$pkg" >/dev/null 2>&1; then
        skip "$pkg"
    else
        NEED_PKGS+=("$pkg")
    fi
done

if (( ${#NEED_PKGS[@]} )); then
    echo "  instalando: ${NEED_PKGS[*]}"
    sudo apt-get update -qq
    sudo apt-get install -y "${NEED_PKGS[@]}"
    ok "pacotes instalados."
else
    ok "todos os pacotes já presentes."
fi

# 2. docker via integração Docker Desktop -----------------------------------
step "2/5  Docker (via integração Docker Desktop)"

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    ok "docker funcional ($(docker --version))."
else
    err "docker não disponível ou daemon offline."
    echo "  No Windows: abra o Docker Desktop, vá em Settings > Resources > WSL Integration"
    echo "  e habilite a integração com esta distro. Depois rode este script de novo."
    exit 1
fi

# 3. variáveis de ambiente (~/.bashrc) --------------------------------------
step "3/5  Variáveis de ambiente (JAVA_HOME, ANDROID_HOME, PATH)"

if [[ -z "$WIN_USER" ]]; then
    # tenta detectar via /mnt/c/Users
    candidates=()
    while IFS= read -r -d '' d; do
        u=$(basename "$d")
        case "$u" in
            Default|Default*|Public|All*Users|defaultuser0) continue ;;
        esac
        if [[ -d "$d/AppData/Local/Android/Sdk" ]]; then
            candidates+=("$u")
        fi
    done < <(find /mnt/c/Users -maxdepth 1 -mindepth 1 -type d -print0 2>/dev/null)

    if (( ${#candidates[@]} == 1 )); then
        WIN_USER="${candidates[0]}"
        ok "detectado usuário Windows com Android SDK: $WIN_USER"
    elif (( ${#candidates[@]} > 1 )); then
        echo "  vários usuários Windows com Android SDK encontrados:"
        for u in "${candidates[@]}"; do echo "    - $u"; done
        read -r -p "  qual usar? " WIN_USER
    else
        read -r -p "  digite seu nome de usuário do Windows: " WIN_USER
    fi
fi

ANDROID_HOME_PATH="/mnt/c/Users/$WIN_USER/AppData/Local/Android/Sdk"
JAVA_HOME_PATH="/usr/lib/jvm/java-21-openjdk-amd64"

if [[ ! -d "$ANDROID_HOME_PATH" ]]; then
    warn "ANDROID_HOME não existe: $ANDROID_HOME_PATH"
    warn "Abra o Android Studio no Windows ao menos uma vez para baixar o SDK."
    if ! confirm "Continuar mesmo assim?" "N"; then exit 1; fi
fi
if [[ ! -d "$JAVA_HOME_PATH" ]]; then
    err "JDK 21 não encontrado em $JAVA_HOME_PATH (esperado após apt install openjdk-21-jdk)."
    exit 1
fi

BASHRC="$HOME/.bashrc"
MARKER_BEGIN="# >>> edu-kt setup >>>"
MARKER_END="# <<< edu-kt setup <<<"

# remove bloco antigo, se houver
if grep -qF "$MARKER_BEGIN" "$BASHRC" 2>/dev/null; then
    sed -i "/$MARKER_BEGIN/,/$MARKER_END/d" "$BASHRC"
fi

cat >> "$BASHRC" <<EOF
$MARKER_BEGIN
export JAVA_HOME="$JAVA_HOME_PATH"
export ANDROID_HOME="$ANDROID_HOME_PATH"
export PATH="\$JAVA_HOME/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator:\$PATH"
$MARKER_END
EOF

ok "bloco edu-kt gravado em ~/.bashrc"

# exportar para esta sessão
export JAVA_HOME="$JAVA_HOME_PATH"
export ANDROID_HOME="$ANDROID_HOME_PATH"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# 4. clone do repo ----------------------------------------------------------
step "4/5  Repositório do projeto"

if [[ -d "$REPO_DIR/.git" ]]; then
    ok "repo já existe em $REPO_DIR"
elif (( DO_CLONE == 0 )); then
    skip "--no-clone passado; pulando clone."
else
    if [[ -z "$REPO_URL" ]]; then
        read -r -p "  URL do repositório git (ex.: git@github.com:org/edu-kt.git): " REPO_URL
    fi
    if [[ -z "$REPO_URL" ]]; then
        warn "sem URL fornecida, pulando clone. Clone manualmente em $REPO_DIR depois."
    else
        git clone "$REPO_URL" "$REPO_DIR"
        ok "clonado em $REPO_DIR"
    fi
fi

# 5. validação + ofertar make dev -------------------------------------------
step "5/5  Validação"

echo "  java:    $(java -version 2>&1 | head -n1 || echo 'ausente')"
echo "  python:  $(python3 --version 2>&1 || echo 'ausente')"
echo "  docker:  $(docker --version 2>&1 || echo 'ausente')"
echo "  adb:     $(adb --version 2>&1 | head -n1 || echo 'ausente — instale via Android Studio')"
echo "  make:    $(make --version 2>&1 | head -n1 || echo 'ausente')"

if [[ -d "$REPO_DIR" ]] && [[ -f "$REPO_DIR/Makefile" ]]; then
    echo
    if confirm "Rodar 'make doctor' agora?"; then
        ( cd "$REPO_DIR" && make doctor ) || warn "make doctor reportou problemas — revise antes de seguir."
    fi
    if confirm "Rodar 'make dev' agora? (sobe backend + emulador + instala app)" "N"; then
        ( cd "$REPO_DIR" && make dev )
    fi
fi

echo
ok "Setup do lado WSL concluído."
echo "  Para abrir nova shell com as variáveis: 'exec bash' ou 'source ~/.bashrc'."
echo "  Próximos comandos úteis (de dentro de $REPO_DIR):"
echo "    make doctor   - diagnóstico"
echo "    make dev      - backend + emulador + app"
echo "    make run      - rebuild + reinstall"
