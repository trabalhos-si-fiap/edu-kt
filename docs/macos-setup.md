# Rodando o projeto no macOS

Guia passo a passo para subir o **Edu** (frontend Android em Kotlin + backend Python/Django em Docker) numa máquina com macOS — tanto **Apple Silicon (M1/M2/M3/M4)** quanto **Intel**.

A boa notícia: o macOS é, junto com Linux, o ambiente mais tranquilo para esse projeto. Tudo é POSIX, o `Makefile` roda nativo, e o Docker Desktop (ou OrbStack) integra bem. O único ponto que historicamente causa dor é o **emulador Android** — daí a seção dedicada de troubleshooting no fim deste documento.

---

## Pré-requisitos

Vamos instalar tudo via **Homebrew**. Se você ainda não tem:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Em Apple Silicon, o Homebrew vive em `/opt/homebrew/bin/brew`; em Intel, em `/usr/local/bin/brew`. Os instaladores adicionam o `brew` ao PATH automaticamente via `~/.zprofile`. Se estiver usando outro shell (`bash`, `fish`), siga as instruções impressas pelo instalador.

### 1. Ferramentas básicas

```bash
brew install git make python@3.12 coreutils
```

> O `make` que vem com o macOS via Xcode Command Line Tools também funciona — qualquer GNU Make ≥ 3.81 serve.

### 2. JDK 21

```bash
brew install openjdk@21
```

Crie o symlink que faz o macOS reconhecer o JDK no `/usr/libexec/java_home`:

```bash
# Apple Silicon
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Intel
sudo ln -sfn /usr/local/opt/openjdk@21/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

Confirme:

```bash
/usr/libexec/java_home -v 21    # imprime o caminho do JDK 21
java -version                    # OpenJDK 21.x
```

### 3. Android Studio + SDK

```bash
brew install --cask android-studio
```

Abra o Android Studio uma vez e siga o wizard. Marque na instalação:

- **Android SDK**
- **Android SDK Platform 35**
- **Android Virtual Device**
- **Android SDK Build-Tools**
- **Android Emulator**

> Em **Apple Silicon**, baixe imagens de sistema **arm64-v8a** (não x86_64). Imagens x86 rodam em modo emulado e ficam quase inutilizáveis no M1/M2/M3/M4. Mais detalhes na seção de [troubleshooting do emulador](#troubleshooting-do-emulador-android).

### 4. Docker Desktop **ou** OrbStack

Você só precisa de um.

| Opção | Como instalar | Notas |
|---|---|---|
| **Docker Desktop** | `brew install --cask docker` | Padrão da indústria. Pesado em RAM. |
| **OrbStack** (recomendado) | `brew install --cask orbstack` | Mais leve e mais rápido, especialmente em Apple Silicon. Drop-in replacement do Docker Desktop. |

Após instalar, abra o app uma vez para iniciar o daemon e aceitar permissões.

```bash
docker --version
docker compose version
```

---

## Configurar variáveis de ambiente

Adicione ao `~/.zshrc` (ou `~/.bash_profile`, conforme seu shell):

### Apple Silicon (M1/M2/M3/M4)

```bash
cat >> ~/.zshrc <<'EOF'

# JDK 21
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"

# Android SDK
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
EOF
source ~/.zshrc
```

### Intel

Idêntico ao bloco acima — os caminhos não mudam, o Android Studio sempre instala o SDK em `~/Library/Android/sdk`.

### Validação

```bash
java -version       # 21.x
adb --version
emulator -version
docker --version
make --version
python3 --version
```

Se `adb` ou `emulator` não forem encontrados, abra o **Android Studio → Settings → Languages & Frameworks → Android SDK** e confirme o **Android SDK Location**. Ele tem que bater com o `ANDROID_HOME`.

---

## Clonar e subir o projeto

```bash
cd ~
git clone <url-do-repo> edu-kt
cd edu-kt
```

### Fluxo automático (recomendado)

```bash
make doctor    # confere JDK, Docker, porta 8000, AVD e IP atual
make dev       # setup + IP + sobe backend + emulador + instala/abre o app
```

O `make dev`:
1. Grava `local.properties` apontando para o SDK.
2. Detecta o IP da LAN e atualiza `BASE_URL` + `network_security_config.xml`.
3. Sobe o backend com `docker compose up -d --wait`.
4. Inicia o emulador.
5. Instala e abre o app.

**Login seedado:** `admin@admin.local` / `admin`.

### Fluxo do dia a dia

```bash
make backend-up        # subir backend
make configure-ip      # se a rede mudou (Wi-Fi diferente, VPN, etc.)
make run               # build + install + launch
make logs              # logcat filtrado
```

Veja `make help` para a lista completa.

---

## Troubleshooting do emulador Android

> Esta seção existe porque um dev novo perdeu meio dia tentando subir o emulador no macOS. Os sintomas mais comuns e como resolver:

### 1. **Apple Silicon: imagem errada (x86_64 em vez de arm64)**

**Sintoma:** emulador abre, fica preto, trava em "Starting…", ou roda em ~2 fps.

**Causa:** você criou um AVD com imagem **x86_64** num Mac M1/M2/M3/M4. Essas imagens só rodam emuladas e a performance é inviável.

**Solução:** apague o AVD e crie outro com imagem **arm64-v8a**.

```bash
# listar AVDs
emulator -list-avds

# apagar um AVD
avdmanager delete avd -n <nome_do_avd>
```

No Android Studio: **Device Manager → Create Device → Pixel 6 → System Image → aba "Other Images" → escolha API 35 com ABI `arm64-v8a`** (em geral marcada com o ícone de Apple Silicon).

### 2. **Conflito de virtualização com Docker / OrbStack / VirtualBox**

**Sintoma:** `emulator: ERROR: x86_64 emulation currently requires hardware acceleration` ou o emulador fecha sozinho ao iniciar.

**Causa:** em **Intel**, o emulador usa o framework Hypervisor da Apple (HVF) — o mesmo que Docker Desktop e VirtualBox usam. Versões antigas do VirtualBox (≤ 6.0) e drivers HAXM legados conflitam.

**Solução:**
- Desinstale qualquer **HAXM** antigo (não é mais necessário desde 2020):

  ```bash
  sudo /Library/Application\ Support/Intel/HAXM/uninstall.sh 2>/dev/null || true
  ```

- Atualize o **VirtualBox** para 6.1+ ou desinstale se não usa.
- O emulador moderno usa **HVF** automaticamente — confirme na linha de comando:

  ```bash
  emulator -accel-check
  # deve imprimir: "HVF (Hypervisor.framework) is installed and usable."
  ```

Em **Apple Silicon** isso não se aplica — não existe HAXM lá, e Docker/OrbStack usam o mesmo HVF sem conflito real.

### 3. **Permissões do macOS bloqueando o emulador**

**Sintoma:** emulador abre e fecha imediatamente, ou popup "qemu-system não pode ser aberto porque não pode ser verificado".

**Solução:** **Sistema → Privacidade e Segurança** → role até o aviso sobre `qemu-system` (ou `emulator`) e clique em **"Abrir mesmo assim"**. Isso só precisa ser feito uma vez por upgrade do SDK.

### 4. **`adb devices` lista o emulador como `offline`**

```bash
adb kill-server
adb start-server
adb devices
```

Se persistir, reinicie o emulador a frio: **Device Manager → ⋮ → Cold Boot Now**.

### 5. **Emulador inicia, mas o app não conecta no backend** (`CLEARTEXT not permitted` / `Connection refused`)

O IP da LAN mudou. Rode:

```bash
make configure-ip
make run
```

### 6. **Apple Silicon: build do Gradle tenta baixar JDK x86_64**

**Sintoma:** Gradle reclama de arquitetura ou demora muito num "Downloading toolchain".

**Causa:** algum `gradle.properties` ou config do Studio fixando uma toolchain x86.

**Solução:** garanta que `JAVA_HOME` aponta para um OpenJDK **aarch64**:

```bash
java -version
# deve mostrar: OpenJDK ... (build 21.x.x ... aarch64)
```

Se mostrar `x86_64`, reinstale: `brew reinstall openjdk@21`.

### 7. **Performance: emulador travando após Docker subir**

**Causa:** Docker Desktop por padrão reserva 8 GB+ de RAM. Em Macs com 16 GB, sobra pouco para o emulador.

**Solução:**
- **Docker Desktop → Settings → Resources** → reduza memória para **4 GB** (suficiente para o backend deste projeto).
- Ou troque para **OrbStack**, que aloca dinamicamente.

### 8. **Atalho: rodar app num device físico em vez do emulador**

Se o emulador não cooperar, conecte um Android via USB com **Depuração USB** ligada (Configurações → Sobre → toque 7× em "Número da versão" → volta, **Opções de desenvolvedor → Depuração USB**).

```bash
adb devices             # confirme que apareceu
make run                # mesmo fluxo, instala no device físico
```

Em Apple Silicon, o cabo precisa ser USB-C → USB-C ou USB-C → adaptador → USB-A do device.

---

## Trabalhando no editor

### Android Studio (frontend Kotlin)

Quem manda no app Android é o **Android Studio**. Refactor inteligente, preview de Compose, debugger e Device Manager só funcionam direito ali.

1. **File → Open** apontando para a raiz do repo.
2. Espere o Gradle sincronizar (a primeira vez baixa toolchain e dependências — 5-15 min dependendo da banda).
3. Selecione um AVD no menu superior e clique **Run ▶** (ou `Shift+F10`).
4. Debug: **Run → Debug 'app'** (`Shift+F9`).
5. Logcat em **View → Tool Windows → Logcat**, filtrável por `package:br.com.edu`.

### VS Code (backend Python)

```bash
brew install --cask visual-studio-code
```

Extensões recomendadas:
- **Python** (`ms-python.python`) + **Pylance**
- **Ruff** (`charliermarsh.ruff`) — lint/format
- **Docker** (`ms-azuretools.vscode-docker`)
- **Kotlin** (`fwcd.kotlin`) — só syntax highlight, sem refactor inteligente

Abra a pasta com `code .` na raiz do repo. O terminal integrado já vem com `JAVA_HOME` e `ANDROID_HOME` carregados (vindos do `~/.zshrc`).

### IntelliJ IDEA Ultimate (opcional)

Quem prefere um IDE só para frontend e backend pode usar IntelliJ Ultimate com o plugin **Python Community Edition** + suporte nativo a Android.

---

## Referências

- Quick start geral: [`README.md`](../README.md)
- Arquitetura do app: [`docs/architecture.md`](architecture.md)
- Integração com backend: [`docs/backend-integration.md`](backend-integration.md)
- Setup do backend: [`back-end/README.md`](../back-end/README.md)
- Setup no Windows: [`docs/windows-setup.md`](windows-setup.md)
