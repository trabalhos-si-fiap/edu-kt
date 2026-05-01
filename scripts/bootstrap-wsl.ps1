<#
.SYNOPSIS
    Bootstrap interativo do ambiente Windows para o projeto Edu.

.DESCRIPTION
    Prepara o lado Windows da máquina:
      - habilita WSL2 + instala Ubuntu
      - instala Docker Desktop, Android Studio, Git via winget
      - copia o setup-wsl.sh para o home do WSL
      - imprime os próximos passos a executar dentro do Ubuntu

    Idempotente: pode ser executado várias vezes sem quebrar.
    Componentes já instalados são detectados e pulados.

.PARAMETER SkipApps
    Pula a instalação de Docker Desktop e Android Studio (útil se já estão
    instalados por outra rota, como Microsoft Store ou MDM corporativo).

.PARAMETER WslDistro
    Nome da distro WSL a instalar/usar. Default: Ubuntu.

.EXAMPLE
    .\scripts\bootstrap-wsl.ps1
    .\scripts\bootstrap-wsl.ps1 -SkipApps
    .\scripts\bootstrap-wsl.ps1 -WslDistro Ubuntu-22.04
#>
[CmdletBinding()]
param(
    [switch]$SkipApps,
    [string]$WslDistro = 'Ubuntu'
)

$ErrorActionPreference = 'Stop'

function Write-Step  { param($msg) Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Ok    { param($msg) Write-Host "  [ok] $msg"   -ForegroundColor Green }
function Write-Skip  { param($msg) Write-Host "  [skip] $msg" -ForegroundColor DarkGray }
function Write-Warn2 { param($msg) Write-Host "  [warn] $msg" -ForegroundColor Yellow }
function Write-Err   { param($msg) Write-Host "  [err] $msg"  -ForegroundColor Red }

function Test-Admin {
    $id = [Security.Principal.WindowsIdentity]::GetCurrent()
    $p = New-Object Security.Principal.WindowsPrincipal($id)
    return $p.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Confirm-YesNo {
    param([string]$Question, [string]$Default = 'Y')
    $suffix = if ($Default -eq 'Y') { '[Y/n]' } else { '[y/N]' }
    while ($true) {
        $ans = Read-Host "$Question $suffix"
        if ([string]::IsNullOrWhiteSpace($ans)) { $ans = $Default }
        switch -Regex ($ans.Trim().ToLower()) {
            '^(y|yes|s|sim)$' { return $true }
            '^(n|no|nao|não)$' { return $false }
            default { Write-Warn2 'Responda y ou n.' }
        }
    }
}

function Test-Winget {
    return [bool](Get-Command winget -ErrorAction SilentlyContinue)
}

function Install-WingetPackage {
    param([string]$Id, [string]$Name)
    $installed = winget list --id $Id --exact 2>$null | Select-String -Pattern $Id -Quiet
    if ($installed) {
        Write-Skip "$Name já instalado."
        return
    }
    Write-Host "  instalando $Name ($Id)..."
    winget install --id $Id --exact --silent --accept-package-agreements --accept-source-agreements | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Ok "$Name instalado." }
    else { Write-Warn2 "winget retornou código $LASTEXITCODE para $Name. Verifique manualmente." }
}

# ----------------------------------------------------------------------------

Write-Host @"

  Edu - Bootstrap do ambiente Windows
  -------------------------------------
  Vai preparar o lado Windows: WSL2 + Ubuntu, Docker Desktop, Android Studio.
  Em seguida, copia um script para o WSL que finaliza o setup do lado Linux.

"@ -ForegroundColor White

if (-not (Test-Admin)) {
    Write-Err 'Este script precisa ser executado como Administrador.'
    Write-Host '  Abra o PowerShell com "Executar como administrador" e rode novamente.'
    exit 1
}

if (-not (Test-Winget)) {
    Write-Err 'winget não encontrado. Atualize o "App Installer" pela Microsoft Store.'
    exit 1
}

# 1. WSL2 ------------------------------------------------------------------
Write-Step '1/4  WSL2 + Ubuntu'

$wslInstalled = $false
try {
    $null = wsl --status 2>$null
    if ($LASTEXITCODE -eq 0) { $wslInstalled = $true }
} catch {}

if (-not $wslInstalled) {
    if (Confirm-YesNo 'WSL ainda não está habilitado. Habilitar agora?') {
        wsl --install --no-distribution
        Write-Warn2 'O Windows precisa reiniciar para concluir a instalação do WSL.'
        Write-Warn2 'Após o reboot, rode novamente este script para continuar.'
        if (Confirm-YesNo 'Reiniciar agora?' 'N') { Restart-Computer -Force }
        exit 0
    } else {
        Write-Err 'WSL é necessário (Docker Desktop depende dele). Abortando.'
        exit 1
    }
} else {
    Write-Ok 'WSL já habilitado.'
}

# Garantir versão 2 default
wsl --set-default-version 2 | Out-Null

# Distro
$distros = (wsl -l -q 2>$null) -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ }
if ($distros -notcontains $WslDistro) {
    if (Confirm-YesNo "Distro '$WslDistro' não encontrada. Instalar?") {
        wsl --install -d $WslDistro
        Write-Warn2 "Uma janela do Ubuntu deve ter aberto. Defina usuário e senha."
        Write-Host '  Pressione Enter aqui depois de concluir o setup inicial do Ubuntu.'
        [void][Console]::ReadLine()
    }
} else {
    Write-Ok "Distro '$WslDistro' já instalada."
}

# 2. Aplicações Windows ----------------------------------------------------
Write-Step '2/4  Aplicações Windows (Docker Desktop, Android Studio, Git)'

if ($SkipApps) {
    Write-Skip 'SkipApps ativo, pulando instalação via winget.'
} else {
    Install-WingetPackage -Id 'Git.Git'                -Name 'Git for Windows'
    Install-WingetPackage -Id 'Docker.DockerDesktop'   -Name 'Docker Desktop'
    Install-WingetPackage -Id 'Google.AndroidStudio'   -Name 'Android Studio'
}

Write-Host ''
Write-Warn2 'Antes de prosseguir, garanta que:'
Write-Host  '   1. Docker Desktop foi iniciado pelo menos uma vez (ícone da baleia verde na bandeja).'
Write-Host  "   2. Em Docker Desktop > Settings > Resources > WSL Integration, a integração com '$WslDistro' está ligada."
Write-Host  '   3. Android Studio foi aberto uma vez para aceitar licenças e baixar o SDK Platform 35 + AVD.'
Write-Host ''
if (-not (Confirm-YesNo 'Os três passos acima já foram concluídos?' 'N')) {
    Write-Host '  Sem problemas — finalize os três passos e rode este script novamente.'
    exit 0
}

# 3. Copiar setup-wsl.sh para o WSL ----------------------------------------
Write-Step '3/4  Copiar setup-wsl.sh para o home do WSL'

$repoRoot = Split-Path -Parent $PSScriptRoot
$srcShell = Join-Path $repoRoot 'scripts\setup-wsl.sh'
if (-not (Test-Path $srcShell)) {
    Write-Err "Não encontrei $srcShell. O repo está completo?"
    exit 1
}

# Converter caminho Windows -> WSL e copiar
$wslSrc = (& wsl -d $WslDistro wslpath -a "$srcShell").Trim()
& wsl -d $WslDistro -- bash -lc "cp '$wslSrc' ~/setup-wsl.sh && chmod +x ~/setup-wsl.sh"
if ($LASTEXITCODE -eq 0) {
    Write-Ok "setup-wsl.sh copiado para ~/setup-wsl.sh dentro de $WslDistro."
} else {
    Write-Err 'Falha ao copiar setup-wsl.sh para o WSL.'
    exit 1
}

# 4. Próximos passos -------------------------------------------------------
Write-Step '4/4  Próximos passos (do lado WSL)'

$winUser = $env:USERNAME
Write-Host ''
Write-Host '  Abra o terminal Ubuntu (menu Iniciar -> Ubuntu) e rode:' -ForegroundColor White
Write-Host ''
Write-Host "    ~/setup-wsl.sh --windows-user '$winUser'" -ForegroundColor Yellow
Write-Host ''
Write-Host '  Esse script vai:' -ForegroundColor White
Write-Host '    - instalar make, python3, openjdk-21-jdk e git no Ubuntu'
Write-Host "    - apontar ANDROID_HOME para o SDK do Windows ($winUser)"
Write-Host '    - clonar (ou usar) o repo em ~/edu-kt'
Write-Host '    - oferecer rodar `make doctor` e `make dev` no final'
Write-Host ''
Write-Ok 'Bootstrap do lado Windows concluído.'
