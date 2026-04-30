# Fluxo de desenvolvimento

Atalhos e ferramentas para iterar rápido. Setup e build cru estão no [README principal](../README.md).

## Makefile

`make help` lista tudo. Os mais usados:

| Comando | Faz |
|---|---|
| `make doctor` | Verifica JDK, Android SDK e `adb devices` |
| `make setup` | Gera `local.properties` apontando para o SDK |
| `make emulator` | Sobe o primeiro AVD em background (use `AVD=nome make emulator` para escolher) |
| `make run` | Build + install + launch |
| `make logs` | Logcat filtrado pelo PID do app |
| `make stop` | force-stop |
| `make clean` | Limpa artefatos |

`JAVA_HOME` e `ANDROID_HOME` são exportados pelo próprio Makefile (Temurin 21 via asdf, `~/Android/Sdk`). Sobrescreva passando na linha de comando: `JAVA_HOME=/outro/jdk make run`.

## Hot reload e iteração rápida

Não há hot reload via CLI — a iteração rápida vem do **Android Studio**.

| Cenário | Ferramenta | Tempo típico |
|---|---|---|
| Mudou layout/cor/texto de um Composable | **Live Edit** (Settings → Editor → Live Edit, modo `Automatic`) | <1s |
| Iterar uma tela isolada sem rodar o app | **`@Preview`** + Interactive Mode | <1s |
| Mudou data class, função normal, dependência | **Apply Changes** (Ctrl+F10) | 2–5s |
| Mudou Manifest, Gradle, recurso | `make run` | 10–15s |

### Adicionar uma Preview

```kotlin
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    EduTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateRegister = {}, onNavigateLogistics = {})
    }
}
```

Abra o arquivo no Android Studio → painel **Split** ou **Design** → ative **Interactive Mode** ou **Live Edit Preview**.

## Verificações antes de subir mudança

1. `make compile` — Kotlin compila sem erro.
2. `make lint` — Android Lint não acusa nada bloqueante.
3. Rodar a tela alterada no emulador via `make run` e validar manualmente o caminho feliz e os caminhos de erro.
4. Quando entrar testes, adicionar `make test` aqui.

## Conventional Commits

O projeto Flutter de origem segue Conventional Commits — manter aqui também:

```
<type>(<scope>): descrição em inglês, imperativo, minúsculas
```

Tipos: `feat`, `fix`, `test`, `refactor`, `docs`. Um commit por unidade lógica — se dá pra dividir, divide.
