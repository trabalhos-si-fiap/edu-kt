# Arquitetura

Organização **feature-first** espelhando o projeto Flutter de origem (`estuda_app`). Cada feature contém suas próprias telas, componentes específicos e (futuramente) repositórios.

## Estrutura de pastas

```
app/src/main/java/br/com/edu/
├── MainActivity.kt              # NavHost + EduTheme
├── core/
│   ├── theme/                   # Design system (cores, tipografia, shapes, gradientes)
│   └── ui/                      # Componentes reutilizáveis em todas as features
└── features/
    ├── auth/
    │   └── presentation/        # LoginScreen, RegisterScreen
    └── marketplace/
        └── presentation/        # MarketplaceScreen, CheckoutScreen,
                                 # OrdersScreen, AddPaymentMethodScreen
```

Quando entrar back-end, cada feature ganha:

```
features/<feature>/
├── data/        # DTOs, retrofit/ktor services, mappers
├── domain/      # Entidades de negócio, use cases, repositórios (interface)
└── presentation/ # Composables + viewmodels
```

## Camadas

| Camada | Responsabilidade | Pode importar |
|---|---|---|
| `presentation` | Composables, ViewModels, estado de UI | `domain`, `core` |
| `domain` | Entidades, regras de negócio, contratos de repositório | nada do projeto (puro Kotlin) |
| `data` | Implementações de repositório, DTOs, fontes de dados | `domain`, `core` |
| `core` | Tema, componentes, utilitários compartilhados | nada de feature |

Regra: **nunca importar de outra `features/X` em uma `features/Y`**. Se há algo compartilhado, sobe para `core`.

## Convenções de código

- **Nomes**: `PascalCase` para classes/composables, `camelCase` para funções e propriedades, `SCREAMING_SNAKE_CASE` apenas para constantes top-level.
- **Composables públicos** começam com letra maiúscula (`LoginScreen`); privados com letra minúscula (`labeledField`) só quando convertidos a função normal — para `@Composable` privados, manter PascalCase com modificador `private`.
- **Tamanho de arquivo**: alvo ~250 linhas. Acima disso, extrair composables auxiliares como `private @Composable fun` no mesmo arquivo ou subir para `core/ui` se virar reutilizável.
- **Estado**: `remember` para estado efêmero, `rememberSaveable` para o que sobrevive a `process death` (ex.: campos de formulário). Para estado de operação (loading/erro/dados do backend), `ViewModel` expondo `StateFlow<UiState>` — padrão já adotado em `LoginViewModel`, `RegisterViewModel`, `MarketplaceViewModel`.
- **Modifiers**: ordem canônica — `.fillMax* → .padding → .background → .border → .clip → .clickable → .semantics`.
- **Strings hardcoded**: ok no protótipo. Ao internacionalizar, mover para `res/values/strings.xml`.

## Princípios

- **KISS** — três linhas iguais > abstração prematura.
- **Composição > herança** — Compose já força isso; não criar wrappers desnecessários.
- **Source of truth única** — estado mora em um lugar e é passado para baixo via parâmetros; eventos sobem via callbacks (`onClick`, `onValueChange`).
- **Sem lógica de negócio em `@Composable`** — chamadas ao backend ficam em `Repository`, orquestração em `ViewModel`. Telas só observam `StateFlow` e disparam intents.
