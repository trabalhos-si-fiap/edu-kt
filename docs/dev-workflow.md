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
| `make test` | Roda os testes unitários (JVM) |
| `make lint` | Roda Android Lint no debug |
| `make logs` | Logcat filtrado pelo PID do app |
| `make stop` | force-stop |
| `make clean` | Limpa artefatos |

`JAVA_HOME` e `ANDROID_HOME` são exportados pelo próprio Makefile (OpenJDK 21 via asdf, `~/Android/Sdk`). Sobrescreva passando na linha de comando: `JAVA_HOME=/outro/jdk make run`.

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

## Testes unitários

Os testes vivem em `app/src/test/` e rodam na JVM (sem emulador, sem Android framework). Cobrem hoje a lógica de domínio: formatação/validação de cartão, validação de CPF/CNPJ, marca do cartão, formatação de moeda.

```bash
make test                                 # roda toda a suíte
./gradlew testDebugUnitTest               # equivalente sem Make
./gradlew test --tests "br.com.edu.features.payment.domain.CardFormattingTest"
./gradlew test --tests "*isValidCpf*"     # filtra por nome de teste
```

Relatório HTML: `app/build/reports/tests/testDebugUnitTest/index.html`. XML JUnit: `app/build/test-results/testDebugUnitTest/`.

Para rodar um teste isolado no Android Studio: ícone ▶ na margem da função `@Test` ou da classe.

> Composables não são cobertos por estes testes (rodam na JVM, sem Android). Se for necessário testar UI, configurar `androidx.compose.ui.test` em `app/src/androidTest/` (requer device/emulador) ou Robolectric em `app/src/test/`.

## Testes e lint do back-end

Os comandos rodam dentro do container `api` (precisa de `make backend-up` antes), aproveitando o mesmo Python/uv/dependências que servem a API.

| Comando | Faz |
|---|---|
| `make backend-test` | `uv run pytest` (configuração em `back-end/pyproject.toml` e `conftest.py`) |
| `make backend-lint` | `uv run ruff check .` |
| `make backend-format` | `uv run ruff format .` |

Para filtrar testes ou rodar com cobertura, use o shell do container:

```bash
make backend-shell
uv run pytest apps/orders/tests/test_orders.py
uv run pytest -k "checkout"
uv run pytest --cov
```

> Sem Docker: `cd back-end && uv sync && uv run pytest` também funciona, mas exige Postgres acessível conforme `config/settings.py`.

## Verificações antes de subir mudança

1. `make compile` — Kotlin compila sem erro.
2. `make test` — testes unitários do front verdes.
3. `make lint` — Android Lint não acusa nada bloqueante.
4. Se mexeu no back-end: `make backend-lint` e `make backend-test` verdes.
5. Rodar a tela alterada no emulador via `make run` e validar manualmente o caminho feliz e os caminhos de erro.

## Conventional Commits

O projeto Flutter de origem segue Conventional Commits — manter aqui também:

```
<type>(<scope>): descrição em inglês, imperativo, minúsculas
```

Tipos: `feat`, `fix`, `test`, `refactor`, `docs`. Um commit por unidade lógica — se dá pra dividir, divide.
