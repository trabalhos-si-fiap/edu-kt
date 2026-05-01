# Telas

Lista funcional de cada Composable de tela. Para layout visual, ver o [Design System](design-system.md). Para fluxo entre elas, ver [Navegação](navigation.md).

## Login (`features/auth/presentation/LoginScreen.kt`)

- Header: "Edu", título "Bem vindo(a) de volta!" e subtítulo.
- Card central com email, senha (toggle de visibilidade), link "Esqueceu sua senha?", botão **Entrar**, divisor "Ou entre com" e dois botões sociais (Google / Apple) — apenas visuais.
- Créditos da equipe e ano abaixo do card.
- `AuthBottomBar` com abas Entrar / Cadastro.
- **Backend**: `POST /api/auth/login` via `LoginViewModel` → `AuthRepository`. Em sucesso, token é salvo no `TokenStore` e a tela navega para o Marketplace; em erro, mostra o `detail` do servidor em Snackbar.
- Estado de UI: `email`, `password`, `passwordVisible` em `rememberSaveable`. Estado de operação: `LoginUiState(loading, error, success)` no ViewModel.

## Cadastro (`features/auth/presentation/RegisterScreen.kt`)

- Header "Crie sua conta!" + 6 campos: Nome, E-mail, Telefone (apenas dígitos), Data de nascimento (`DatePickerDialog`), Senha, Confirmar senha (ambos com toggle de visibilidade).
- **Validações locais** (antes de bater no backend):
  - Todos os campos obrigatórios.
  - E-mail precisa conter `@`.
  - Senha ≥ 8 caracteres **e** pelo menos 1 caractere especial (`[!@#$%^&*(),.?":{}|<>]`).
  - Confirmação igual à senha.
- **Backend**: se as validações passarem, `RegisterViewModel.submit(...)` chama `POST /api/auth/register`. Em sucesso, snackbar "Cadastro realizado!" + navegação para Login; em erro (ex.: e-mail duplicado), exibe o `detail` retornado.
- A retomada manual para o Login é feita pela `AuthBottomBar`.

## Marketplace (`features/marketplace/presentation/MarketplaceScreen.kt`)

- TopAppBar branca com perfil (esq.) e carrinho (dir.). Sem ícone de notificações.
- `LazyColumn` com:
  1. Título **EduMarketplace**.
  2. Search bar com placeholder em pt-BR ("Buscar cursos, guias ou materiais...").
  3. **Featured card** (bg `Primary`) com tag "EDUCAÇÃO 5.0" e CTA roxo "Explorar Coleção".
  4. Lista de `ProductCard` carregada de `GET /api/products` via `MarketplaceViewModel`. Estados: spinner durante o load, mensagem + botão "Tentar novamente" em erro, lista renderizada em sucesso.
- Cada `ProductCard` exibe **estrelas (1–5)** com média + contagem (`RatingStars`). Toque nas estrelas (quando `ratingCount > 0`) abre o `ReviewsBottomSheet`, que carrega `GET /api/products/{id}/reviews` e renderiza autor, rating e comentário por avaliação.
- `MainBottomBar(selected = 0)` — toque em "Meus Pedidos" / "Perfil" navega.
- O ícone do carrinho na top bar abre `CheckoutScreen`. O badge mostra a quantidade total de itens no carrinho com o número centralizado vertical e horizontalmente no círculo (via `lineHeight`/`LineHeightStyle` + `includeFontPadding=false`).
- O ícone de perfil na top bar (`PersonOutline`) abre `ProfileScreen`.

## Checkout (`features/marketplace/presentation/CheckoutScreen.kt`)

- TopAppBar com seta voltar + título "Finalizar Pedido".
- Seção **Revisão do Carrinho** com 2 `CartItemCard` (badges de categoria coloridos, link "Remover" vermelho).
- Seção **Endereço de Entrega** lista os endereços do usuário (`AddressRepository.list()`) usando `AddressOptionCard` (mesmo visual do `PaymentMethodCard`, com ícone `LocationOn`, badge "FAVORITO" para o endereço favorito, borda roxa + check no selecionado). Pré-seleciona o `isFavorite` ou o primeiro da lista. Sem endereços cadastrados, exibe texto "Você ainda não cadastrou nenhum endereço. Cadastre em Perfil > Endereços." e o checkout segue habilitado.
- Seção **Método de Pagamento** com:
  - `PaymentOption` Cartão de Crédito (Visa final 4492).
  - `PaymentOption` PIX.
  - `DottedBorderBox` "Outro método" → navega para `AddPaymentMethodScreen`.
- Estado: `selectedPayment: Int` em `mutableIntStateOf`. Item selecionado ganha borda roxa 2dp e check-badge.
- `AlertDialog` de confirmação mostra Total, método de pagamento e endereço escolhido (resumo "rua, número — bairro — cidade/UF").

## Adicionar Método de Pagamento (`features/marketplace/presentation/AddPaymentMethodScreen.kt`)

- TopAppBar com seta voltar + título "Adicionar Método".
- Seletor de tipo (3 opções): **Cartão**, **PIX**, **Boleto**.
- Campos por tipo:
  - **Cartão**: número formatado `0000 0000 0000 0000` (via `VisualTransformation`), nome em UPPERCASE, validade `MM/AA`, CVV mascarado.
  - **PIX**: chave + caixa verde "Aprovação imediata após o pagamento."
  - **Boleto**: caixa cinza com aviso de compensação.
- Checkbox **Definir como método padrão** (estado local; ainda não persistido).
- Botão roxo **Salvar método** valida cartão (≥13 dígitos, nome, validade ≥4 dígitos, CVV ≥3) e volta com Snackbar. Validação e formatação ficam em `features/payment/domain/CardFormatting.kt` (cobertas por `app/src/test/.../CardFormattingTest.kt`).
- Rodapé "Pagamentos protegidos com criptografia".

## Perfil (`features/profile/presentation/ProfileScreen.kt`)

- TopAppBar com seta voltar + título "Perfil".
- Header com avatar circular roxo (inicial do nome) + nome e e-mail.
- Card **Dados pessoais** com nome, telefone e data de nascimento. Telefone exibido com máscara `(DD) 90000-0000` (helper `formatPhoneBR`). Botão de lápis no canto entra em modo de edição inline com `TextField`s (nome, telefone com `KeyboardType.Phone` + `PhoneVisualTransformation` que aplica a máscara em tempo real e aceita só dígitos até 11, data de nascimento `AAAA-MM-DD`); Salvar dispara `PATCH /api/auth/me`, Cancelar descarta. Erros do servidor aparecem em texto vermelho abaixo dos campos.
- Seção **Atalhos**: cards clicáveis "Meus pedidos" → `OrdersScreen`, "Métodos de pagamento" → `CheckoutScreen` (placeholder até existir tela própria).
- Botão **Sair da conta** (vermelho suave) chama `TokenStore.clear()` e navega para `login` com `popUpTo(0)`.
- `MainBottomBar(selected = 2)` — toque em "Loja" volta ao Marketplace (com `popUpTo("marketplace") { inclusive = true }`), "Meus Pedidos" navega.
- **Backend**: `GET /api/auth/me` no `LaunchedEffect(Unit)` via `ProfileViewModel` → `UserRepository`. Estados: `Loading`, `Ready(profile, isEditing, saving, saveError)`, `Error(message)`.

## Pedidos (`features/marketplace/presentation/OrdersScreen.kt`)

- TopAppBar com apenas a seta de voltar (sem ícones de perfil ou notificações).
- Título "Seus pedidos".
- **Backend**: `GET /api/orders` via `OrdersViewModel` → `OrdersRepository`. Estados: `Loading`, `Ready(orders)` (vazio mostra empty state "Você ainda não fez nenhum pedido"), `Error(message)` com botão "Tentar novamente".
- **Card de pedido entregue** (todos os pedidos retornados pelo backend são tratados como entregues — modelo atual não tem status):
  - Header: `#EDU-NNNNNN` (id zero-paddado em 6 dígitos) + data formatada em pt-BR, badge verde "ENTREGUE".
  - **Carrossel** (`LazyRow`) com card por item (largura 180dp, altura fixa 232dp): imagem (Coil/`SubcomposeAsyncImage` com placeholder), nome (2 linhas reservadas via `minLines = 2`), avaliação (estrela amber + `rating_avg` `(rating_count)` ou "Sem avaliações"), `xN` e preço unitário formatado em BRL.
  - Linha de resumo: total de itens (somando `quantity`) + total formatado.
  - Botão **"Comprar\nnovamente"** (texto em duas linhas) → `viewModel.rebuy(order.id)` → `POST /api/orders/{id}/rebuy` (incrementa carrinho com itens do pedido) → navega para `checkout`.
  - Botão **"Avaliar\nitens"** (texto em duas linhas) → abre `AlertDialog` com seletor de estrelas (`StarPicker` 1–5) por item; submit dispara `POST /api/products/{id}/reviews` por item avaliado (autor derivado de `user.get_full_name()` ou `email`) e recarrega a lista para refletir as novas médias.
  - Os dois botões usam `EduSoftButton` com altura mínima de 48.dp; ambos com 2 linhas garantem altura idêntica lado a lado.
- Eventos de ação (`OrdersAction.RebuySuccess` / `ReviewsSubmitted` / `Error`) saem do ViewModel via `StateFlow<OrdersAction?>`, consumidos no `LaunchedEffect(action)` da tela e exibidos via `SnackbarHost`.
