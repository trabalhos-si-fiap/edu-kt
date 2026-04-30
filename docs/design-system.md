# Design System

Reproduzido 1:1 do app Flutter (`lib/core/theme/app_colors.dart` e `app_theme.dart`). Todos os tokens vivem em `app/src/main/java/br/com/edu/core/theme/`.

## Cores (`Color.kt` → `EduColors`)

| Token | Hex | Uso |
|---|---|---|
| `Purple` | `#5B00DF` | Acentos, links, botões secundários, seleção, foco de input |
| `Primary` | `#1A1A2E` | Botões primários, textos titulares, fundo do featured card |
| `TextPrimary` | `#1A1A2E` | Texto principal |
| `TextSecondary` | `#6B7280` | Texto secundário, hints, ícones inativos |
| `Background` | `#A9CADD` | Topo do gradiente de fundo |
| `White` | `#FFFFFF` | Cards, top app bar, fundo claro |
| `InputFill` | `#F3F4F6` | Background de TextField, botões soft |
| `InputBorder` | `#E5E7EB` | Bordas e divisores |
| `Success` | `#22C55E` | Badge "ENTREGUE", tag "Educação 5.0" |
| `Danger` | `#DC2626` | Link "Remover", erros |
| `PurpleSoft` | `#EDE0FF` | Badges (Pedido ativo, categoria Premium) |
| `GreenSoft` | `#D1F4DD` | Badge categoria Digital, info-box do PIX |
| `GreenDark` | `#15803D` | Texto de badges/info verdes |

Gradiente do fundo (`EduGradients.Background`): `#A9CADD → #BDD5E5 → #D1E0EE` vertical. Usado nas telas de Login e Cadastro.

## Tipografia (`Type.kt`)

Família única: **Lexend Deca** via `androidx.compose.ui:ui-text-google-fonts` (precisa de internet na primeira execução; cai para fallback do sistema offline).

| Estilo Material 3 | Tamanho / peso | Uso |
|---|---|---|
| `displayLarge` | 32sp / ExtraBold | Títulos de tela ("Bem vindo", "EduMarketplace", "Seus pedidos") |
| `headlineMedium` | 26sp / ExtraBold | Títulos secundários ("Revisão do Carrinho") |
| `titleLarge` | 20sp / ExtraBold | Títulos de card |
| `titleMedium` | 18sp / ExtraBold | Títulos de itens do carrinho |
| `bodyLarge` | 15sp / Normal | Subtítulos longos |
| `bodyMedium` | 14sp / Normal | Texto padrão |
| `bodySmall` | 13sp / Normal | Subtítulos curtos, descrições |
| `labelLarge` | 14sp / SemiBold | Labels de formulário |
| `labelSmall` | 12sp / Bold | Tags em caixa alta, créditos |

## Formas (`Shape.kt`)

| Token | Raio | Uso |
|---|---|---|
| `small` | 12.dp | Botões, info-boxes, ícones-container |
| `medium` | 20.dp | Product cards, cart items |
| `large` | 24.dp | Cards principais, top corners da bottom bar |
| `extraLarge` | 32.dp | Search bar |

## Componentes (`core/ui/`)

| Componente | Resumo |
|---|---|
| `EduTextField` | TextField com fill `InputFill`, raio 24, sem borda padrão, foco roxo. Suporta `leadingIcon`, `trailingIcon`, `enabled`, `readOnly`, `visualTransformation`. |
| `EduPrimaryButton` | Botão full-width, bg `Primary`, raio 12, altura mínima 52. |
| `EduPurpleButton` | Botão de destaque (CTA), bg `Purple`, raio 12. |
| `EduSoftButton` | Botão secundário leve, full-width, bg/fg parametrizáveis. |
| `EduCard` | `Surface` com sombra suave, raio 24, padding interno parametrizável. |
| `DottedBorderBox` | Borda tracejada (`Modifier.drawBehind` + `PathEffect`). Usada no "Outro método" do Checkout. |
| `AuthBottomBar` | Barra inferior de 2 itens (Entrar / Cadastro). |
| `MainBottomBar` | Barra inferior de 2 itens (Loja / Meus Pedidos). |

## Diretrizes

- **Espaçamento canônico**: padding horizontal de tela = 24.dp; gap vertical entre seções = 16-24.dp; gap label→input = 8.dp.
- **Sombra**: discreta, `shadowElevation = 4.dp` por padrão. Featured cards podem subir para 8.
- **Sempre via tokens** — não usar `Color(0xFF...)` direto em telas, importar de `EduColors`. O mesmo para tipografia (`MaterialTheme.typography.X`) em vez de `TextStyle` ad-hoc.
