# Rodando o projeto no Windows 10 / 11

Guia passo a passo para subir o **Edu** (frontend Android em Kotlin + backend Python/Django em Docker) numa máquina com Windows 10 ou Windows 11.

## Por que WSL2 é praticamente obrigatório

**Use WSL2.** Não é só uma alternativa "mais elegante" — é o caminho recomendado e, na prática, necessário pelos motivos abaixo:

- **O backend roda em Docker.** No Windows, o Docker Desktop **só funciona em cima do WSL2** (ou Hyper-V em edições Pro/Enterprise, mas o WSL2 é o backend oficial e padrão desde 2022). Sem WSL2 habilitado, o Docker Desktop nem inicia.
- **Performance e compatibilidade do Docker.** Volumes, healthchecks e `docker compose --wait` são significativamente mais rápidos e estáveis quando o repositório vive **dentro** do filesystem do WSL (ex.: `~/edu-kt`) do que quando o Docker fala com arquivos em `C:\` via tradução de filesystem.
- **Paridade com o time.** Todos os scripts (`Makefile`, `scripts/configure_ip.py`, comandos do backend) foram escritos para shell POSIX. No PowerShell/CMD você reescreve cada comando; no WSL você roda exatamente os mesmos `make dev`, `make doctor`, `make run` que o resto do time.
- **Ferramentas Python/uv/Make.** Disponíveis nativamente no Ubuntu do WSL com um `apt install`. No Windows puro, cada uma exige instalador separado e ajustes de PATH.

O **único pedaço que continua rodando do lado Windows** é o **emulador Android** (precisa de aceleração gráfica do host) e o **Android Studio**, que você instala normalmente no Windows. O `adb` do WSL fala com o emulador via rede sem problemas.

Por isso este guia é dividido em dois caminhos:

1. **[Caminho recomendado: WSL2 + Ubuntu](#caminho-recomendado-wsl2--ubuntu)** — fluxo idêntico ao Linux, usa o `Makefile`.
2. **[Caminho alternativo: Windows nativo (PowerShell)](#caminho-alternativo-windows-nativo-powershell)** — só se você tem alguma restrição que impeça o WSL2.

---

## Caminho recomendado: WSL2 + Ubuntu

> **Atalho:** o repo traz dois scripts que automatizam quase todo o setup desta seção:
> - `scripts\bootstrap-wsl.ps1` — rode no **PowerShell como administrador** no Windows. Habilita WSL2, instala Docker Desktop, Android Studio e Git via `winget`, e copia o segundo script pro home do WSL.
> - `~/setup-wsl.sh` — depois, no **terminal Ubuntu**. Instala deps do Ubuntu, configura `JAVA_HOME`/`ANDROID_HOME`, opcionalmente clona o repo e oferece rodar `make dev`.
>
> Ambos são idempotentes (podem ser rodados várias vezes) e interativos. Se preferir entender cada passo manualmente, siga as 8 etapas abaixo.

### 1. Habilitar virtualização

1. Reinicie e entre na BIOS/UEFI (geralmente `F2`, `F10`, `Del` ou `Esc` no boot).
2. Habilite **Intel VT-x / AMD-V** (em alguns BIOS aparece como "SVM Mode" ou "Virtualization Technology").
3. Salve e volte ao Windows.

### 2. Instalar WSL2 + Ubuntu

PowerShell **como administrador**:

```powershell
wsl --install -d Ubuntu
```

Reinicie a máquina. No primeiro boot do Ubuntu, defina usuário e senha do WSL.

Confirme a versão:

```powershell
wsl -l -v          # a coluna VERSION precisa mostrar 2
```

### 3. Instalar Docker Desktop e Android Studio (no Windows)

| Ferramenta | Onde baixar | Observações |
|---|---|---|
| **Docker Desktop** | <https://www.docker.com/products/docker-desktop/> | Em **Settings → Resources → WSL Integration**, habilite a integração com a distro Ubuntu |
| **Android Studio** | <https://developer.android.com/studio> | No setup, deixe marcadas **Android SDK**, **Platform 35**, **Android Virtual Device** |

> Não instale o Android Studio dentro do WSL — ele precisa de aceleração gráfica do Windows.

### 4. Instalar dependências dentro do Ubuntu (WSL)

Abra o terminal **Ubuntu** (menu Iniciar → "Ubuntu"):

```bash
sudo apt update
sudo apt install -y make python3 python3-pip openjdk-21-jdk git curl unzip
```

Confirme:

```bash
java -version       # deve mostrar 21.x
docker --version    # deve funcionar via integração do Docker Desktop
make --version
python3 --version
```

### 5. Apontar o Android SDK do Windows para o WSL

O SDK fica do lado Windows (instalado pelo Android Studio). Exponha-o no WSL:

```bash
echo 'export ANDROID_HOME=/mnt/c/Users/<seu-usuario-windows>/AppData/Local/Android/Sdk' >> ~/.bashrc
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH' >> ~/.bashrc
source ~/.bashrc
```

Substitua `<seu-usuario-windows>` pelo seu nome de usuário do Windows.

Validação:

```bash
adb --version       # listar Android Debug Bridge
```

### 6. Clonar o projeto **dentro** do WSL

> ⚠️ Clone em `~/` (filesystem nativo do WSL), **não** em `/mnt/c/...`. Builds em `/mnt/c` são até 10× mais lentos.

```bash
cd ~
git clone <url-do-repo> edu-kt
cd edu-kt
```

### 7. Subir tudo com o `Makefile`

```bash
make doctor    # confere JDK, Docker, porta 8000, AVD e IP atual
make dev       # setup + IP + sobe backend + emulador + instala/abre o app
```

Pronto. O `make dev` vai:
1. gravar o `local.properties` apontando para o SDK,
2. detectar o IP da LAN e atualizar `BASE_URL` + `network_security_config.xml`,
3. subir o backend com `docker compose up -d --wait`,
4. iniciar o emulador (do lado Windows, via `adb` em rede),
5. instalar e abrir o app.

**Login seedado:** `admin@admin.local` / `admin`.

### 8. Fluxo do dia a dia

```bash
make backend-up        # subir backend
make configure-ip      # se a rede mudou
make run               # build + install + launch
make logs              # logcat filtrado
```

Veja `make help` para a lista completa.

### Troubleshooting WSL2

| Sintoma | Solução |
|---|---|
| `docker: command not found` no Ubuntu | Habilite a integração WSL no Docker Desktop (**Settings → Resources → WSL Integration**) e reabra o terminal. |
| `adb devices` não lista o emulador | O `adb` do Windows e do WSL podem brigar. Mate o do Windows (`adb kill-server` no PowerShell) e deixe só o do WSL. |
| Build do Gradle absurdamente lento | O projeto está em `/mnt/c/...`. Mova para `~/edu-kt`. |
| `make` não encontra o SDK | Confira que `ANDROID_HOME` aponta para um caminho que existe via `ls $ANDROID_HOME`. |
| Emulador trava ou não abre | Suba o emulador pelo **Android Studio (Windows)**; o WSL só precisa que ele apareça em `adb devices`. |

---

## Caminho alternativo: Windows nativo (PowerShell)

Use somente se houver algum bloqueio para o WSL2 (política de TI, hardware sem virtualização, etc.). Você ainda vai precisar do Docker Desktop, que ainda assim usa WSL2 por baixo dos panos — então a economia é menor do que parece.

### 1. Pré-requisitos

Instale, na ordem abaixo. Reinicie a máquina ao final.

| # | Ferramenta | Versão | Onde baixar / como instalar |
|---|---|---|---|
| 1 | **Git for Windows** | mais recente | <https://git-scm.com/download/win> — aceite os defaults; isso instala também o `Git Bash` |
| 2 | **JDK 21 (Temurin)** | 21 LTS | <https://adoptium.net/temurin/releases/?version=21> — escolha o instalador `.msi` x64 e marque **"Set JAVA_HOME"** e **"Add to PATH"** |
| 3 | **Android Studio** | Hedgehog (2023.1) ou superior | <https://developer.android.com/studio> — durante o setup, deixe marcadas as opções **Android SDK**, **Android SDK Platform 35**, **Android Virtual Device** |
| 4 | **Docker Desktop** | mais recente | <https://www.docker.com/products/docker-desktop/> — exige Windows 10 22H2+ ou Windows 11 |
| 5 | **Python 3.10+** | 3.10 ou superior | <https://www.python.org/downloads/windows/> — marque **"Add python.exe to PATH"** no instalador |

> **Não precisa instalar Gradle** — o projeto traz o `gradlew.bat`, que baixa a versão correta sozinho.

### 2. Configurar variáveis de ambiente

Abra **Configurações → Sistema → Sobre → Configurações avançadas do sistema → Variáveis de ambiente**.

Em **Variáveis de usuário**, garanta:

| Variável | Valor de exemplo |
|---|---|
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot` |
| `ANDROID_HOME` | `C:\Users\<seu-usuario>\AppData\Local\Android\Sdk` |

Edite o `Path` (na mesma janela) e adicione:

```
%JAVA_HOME%\bin
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\emulator
%ANDROID_HOME%\cmdline-tools\latest\bin
```

Feche e reabra todos os terminais. Validação no PowerShell:

```powershell
java -version          # deve mostrar 21.x
adb --version
docker --version
python --version
```

### 3. Clonar o projeto

```powershell
cd $HOME
git clone <url-do-repo> edu-kt
cd edu-kt
```

### 4. Subir o backend

Com o **Docker Desktop aberto** (ícone da baleia verde):

```powershell
cd back-end
docker compose up -d --wait
cd ..
```

API em `http://localhost:8000`. Login seedado: `admin@admin.local` / `admin`.

### 5. Configurar o IP da LAN

```powershell
python scripts\configure_ip.py
python scripts\configure_ip.py --ip 192.168.0.10   # forçar IP
python scripts\configure_ip.py --show              # mostrar atual
```

> **Importante:** rode esse comando sempre que seu IP mudar (Wi-Fi, VPN, reboot do roteador). Sem isso, o app falha com `CLEARTEXT not permitted` ou `Connection refused`.

### 6. Subir o emulador

Pelo Android Studio: **More Actions → Virtual Device Manager → Create Device** (Pixel 6, API 35) e clique em ▶.

Por linha de comando:

```powershell
emulator -list-avds
Start-Process -NoNewWindow emulator -ArgumentList "-avd", "<nome_do_avd>"
adb devices                # confere
```

### 7. Build e install

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
adb shell am start -n br.com.edu/.MainActivity
```

Build limpo:

```powershell
.\gradlew.bat --stop
.\gradlew.bat clean assembleDebug
```

### 8. Fluxo do dia a dia

```powershell
cd back-end ; docker compose up -d --wait ; cd ..
python scripts\configure_ip.py
.\gradlew.bat installDebug
adb shell am start -n br.com.edu/.MainActivity
adb logcat | findstr br.com.edu
```

### Troubleshooting Windows nativo

| Sintoma | Causa / Solução |
|---|---|
| `gradlew : The term 'gradlew' is not recognized` | Use `.\gradlew.bat` no PowerShell (o ponto-barra é obrigatório). |
| `JAVA_HOME is set to an invalid directory` | Confira o caminho exato em `C:\Program Files\Eclipse Adoptium\` — versões diferem. |
| `SDK location not found` | Crie `local.properties` na raiz com `sdk.dir=C\:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk` (barras duplas e `\:`). |
| `Unsupported class file major version` | JDK menor que 17. Reinstale o Temurin 21 e refaça as variáveis de ambiente. |
| `CLEARTEXT communication not permitted` | Rode `python scripts\configure_ip.py` novamente — o IP mudou. |
| `docker compose` retorna `error during connect` | Docker Desktop não está rodando. |
| Emulador não aparece em `adb devices` | `adb kill-server` seguido de `adb start-server`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | `adb uninstall br.com.edu` e reinstale. |
| Build trava ou erro de cache | `.\gradlew.bat --stop` e `Remove-Item -Recurse -Force $HOME\.gradle\caches\build-cache-1`. |
| Antivírus bloqueia o build | Adicione exceção para `%USERPROFILE%\.gradle` e a pasta do projeto no Windows Defender. |
| Porta 8000 ocupada | `netstat -ano \| findstr :8000` e `taskkill /PID <pid> /F`. |

---

## Trabalhando no editor (VS Code, Android Studio, IntelliJ)

O projeto tem dois "lados" que pedem ferramentas diferentes:

- **App Android (Kotlin + Jetpack Compose)** — quem manda é o **Android Studio** (ou IntelliJ IDEA). Ele entende Gradle, fornece preview de Compose, debugger conectado ao emulador e gerenciamento de AVDs. Use VS Code para Kotlin só se for editar pontual.
- **Backend (Python + Django)** — **VS Code** funciona muito bem, especialmente conectado ao WSL via **Remote - WSL**. PyCharm também é uma opção sólida.

### VS Code conectado ao WSL (recomendado se você seguiu o caminho WSL2)

1. Instale o **VS Code no Windows** (não no WSL): <https://code.visualstudio.com>.
2. Instale a extensão **Remote - WSL** (`ms-vscode-remote.remote-wsl`).
3. Abra o terminal Ubuntu e, **dentro** da pasta do repo no WSL, rode:

   ```bash
   cd ~/edu-kt
   code .
   ```

   O VS Code abre conectado ao WSL — todas as extensões instaladas a partir daí rodam do lado Linux, com acesso direto a `make`, `docker`, `python3` e `./gradlew`.

4. Extensões recomendadas (instale com a tag "in WSL: Ubuntu"):
   - **Python** (`ms-python.python`) + **Pylance** — backend
   - **Ruff** (`charliermarsh.ruff`) — lint/format do backend
   - **Kotlin** (`fwcd.kotlin`) — syntax highlight razoável; sem refactor inteligente
   - **Gradle for Java** (`vscjava.vscode-gradle`) — disparar tasks pelo painel
   - **Docker** (`ms-azuretools.vscode-docker`) — gerenciar containers do compose
   - **GitLens** (`eamodio.gitlens`) — opcional, mas útil

5. Abra um terminal integrado (`Ctrl+'`). Ele já vem como bash do Ubuntu, com `JAVA_HOME`/`ANDROID_HOME` carregados (se você rodou o `setup-wsl.sh` ou seguiu o passo 5 do guia).

> **Não** use a extensão "Remote - WSL" para abrir um diretório em `/mnt/c/...`. Prefira sempre `~/edu-kt` (filesystem nativo do WSL) — é onde o Gradle e o Docker performam bem.

### VS Code no Windows nativo (sem WSL)

Funciona, mas é o cenário com mais armadilhas. Recomendado só se você está no caminho alternativo (Windows nativo).

1. Abra a pasta do repo direto: `code C:\Users\<usuario>\edu-kt`.
2. O VS Code usa o terminal padrão do Windows. Se preferir PowerShell, configure em `Terminal: Default Profile` → "PowerShell".
3. Mesmas extensões da seção acima, com duas observações:
   - A extensão **Python** vai pedir o interpretador — aponte para o `python.exe` que você instalou.
   - Tasks do Gradle precisam usar `gradlew.bat`, não `./gradlew`. Configure em `.vscode/tasks.json` se usar muito.

### Rodar/debugar o app Android — use Android Studio

Para qualquer trabalho sério no frontend Kotlin (refactor, preview de Compose, debugger), abra o projeto no **Android Studio**:

1. **File → Open** e aponte para a raiz do repo.
2. Espere o Gradle sincronizar (o Studio usa o JDK que ele mesmo gerencia, separado do `JAVA_HOME` do sistema).
3. Selecione um AVD em execução no menu superior e clique no botão **Run ▶** (ou `Shift+F10`).
4. Para debug com breakpoints: **Run → Debug 'app'** (ou `Shift+F9`).
5. Logcat fica em **View → Tool Windows → Logcat**, filtrável por `package:br.com.edu`.

> No caminho **WSL2**, abra o Android Studio do lado **Windows** mesmo, mas aponte para a pasta dentro do WSL via o caminho UNC: `\\wsl$\Ubuntu\home\<usuario>\edu-kt`. O Studio funciona, mas os builds ficam mais lentos do que rodar `make run` no terminal do WSL — então uma combinação comum é: **edição/preview no Studio, build/install via `make` no terminal**.

### IntelliJ IDEA Ultimate (opcional)

Quem já usa IntelliJ pode abrir o projeto direto — ele instala o plugin Android sob demanda. Mesmo trabalho do Android Studio, mas com suporte melhor para o backend Python no mesmo IDE (instale o plugin **Python Community Edition**).

### Configuração compartilhada (`.vscode/`)

Hoje o repo **não** versiona `.vscode/settings.json` ou `launch.json` — cada dev configura o próprio editor. Se você criar configs locais, mantenha em `.vscode/` (já está no `.gitignore` por padrão na maioria dos templates) ou abra um PR adicionando um `.vscode/settings.json.example` para o time.

---

## Referências

- Quick start geral: [`README.md`](../README.md)
- Arquitetura do app: [`docs/architecture.md`](architecture.md)
- Integração com backend: [`docs/backend-integration.md`](backend-integration.md)
- Setup do backend: [`back-end/README.md`](../back-end/README.md)
