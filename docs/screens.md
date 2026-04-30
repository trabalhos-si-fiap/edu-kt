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

- TopAppBar branca com perfil (esq.), carrinho e notificações (dir.).
- `LazyColumn` com:
  1. Título **EduMarketplace**.
  2. Search bar (apenas UI, sem filtro real).
  3. **Featured card** (bg `Primary`) com tag "EDUCAÇÃO 5.0" e CTA roxo "Explorar Coleção".
  4. Lista de `ProductCard` carregada de `GET /api/products` via `MarketplaceViewModel`. Estados: spinner durante o load, mensagem + botão "Tentar novamente" em erro, lista renderizada em sucesso.
- `MainBottomBar(selected = 0)` — toque em "Meus Pedidos" navega.
- O ícone do carrinho na top bar abre `CheckoutScreen`.

## Checkout (`features/marketplace/presentation/CheckoutScreen.kt`)

- TopAppBar com seta voltar + título "Finalizar Pedido".
- Seção **Revisão do Carrinho** com 2 `CartItemCard` (badges de categoria coloridos, link "Remover" vermelho).
- Seção **Método de Pagamento** com:
  - `PaymentOption` Cartão de Crédito (Visa final 4492).
  - `PaymentOption` PIX.
  - `DottedBorderBox` "Outro método" → navega para `AddPaymentMethodScreen`.
- Estado: `selectedPayment: Int` em `mutableIntStateOf`. Item selecionado ganha borda roxa 2dp e check-badge.

## Adicionar Método de Pagamento (`features/marketplace/presentation/AddPaymentMethodScreen.kt`)

- TopAppBar com seta voltar + título "Adicionar Método".
- Seletor de tipo (3 opções): **Cartão**, **PIX**, **Boleto**.
- Campos por tipo:
  - **Cartão**: número formatado `0000 0000 0000 0000` (via `VisualTransformation`), nome em UPPERCASE, validade `MM/AA`, CVV mascarado.
  - **PIX**: chave + caixa verde "Aprovação imediata após o pagamento."
  - **Boleto**: caixa cinza com aviso de compensação.
- Checkbox **Definir como método padrão** (estado local; ainda não persistido).
- Botão roxo **Salvar método** valida cartão (≥13 dígitos, nome, validade ≥4 dígitos, CVV ≥3) e volta com Snackbar.
- Rodapé "Pagamentos protegidos com criptografia".

## Pedidos (`features/marketplace/presentation/OrdersScreen.kt`)

- TopAppBar com seta voltar + perfil + sino.
- Título "Seus pedidos".
- **Pedido ativo**:
  - Badge `Pedido ativo` (`PurpleSoft`).
  - ID, data de compra e total destacado.
  - **OrderStepper** (3 etapas: Separação, Trânsito, Entregue) — etapa atual e anteriores ficam roxas; pendentes em cinza.
  - Caixa cinza com data prevista e localização do pacote.
  - Botão "Detalhes do pedido" (placeholder).
- **Pedido entregue**: card com bg `InputFill`, badge verde "ENTREGUE", thumbnails dos itens, contagens, botões "Comprar novamente" (roxo claro) e "Avaliar itens" (branco).
- Enum `OrderStep { Picking, Transit, Delivered }`.
