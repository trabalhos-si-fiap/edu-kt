# Navegação

Implementada com [`androidx.navigation:navigation-compose`](https://developer.android.com/jetpack/compose/navigation). O `NavHost` é montado em `MainActivity.kt::EduApp` (`app/src/main/java/br/com/edu/MainActivity.kt:34-140`).

Esta página cobre os quatro conceitos exigidos pela Fase 3: **Scaffold**, **componentes de navegação**, **gerenciamento de rotas** e **troca de dados entre telas**.

## 1. Scaffold

O `Scaffold` do Material 3 é usado em todas as telas que precisam de top bar, bottom bar ou snackbar. Ele resolve:

- **Slots fixos** (`topBar`, `bottomBar`, `snackbarHost`, `floatingActionButton`) que mantêm o layout consistente entre telas.
- **`innerPadding`** propagado para o conteúdo, garantindo que o conteúdo nunca fique embaixo das barras.
- **Ancoragem do `SnackbarHost`** ao próprio Scaffold, evitando overlap com bottom bar.

Exemplos no código:

| Tela | Scaffold | Top bar | Bottom bar |
|---|---|---|---|
| `MarketplaceScreen` | sim (`MarketplaceScreen.kt:126`) | `MarketplaceTopBar` (busca + carrinho) | `MainBottomBar(selected = 0)` |
| `OrdersScreen` | sim | seta voltar + título | `MainBottomBar(selected = 1)` |
| `SupportScreen` | sim | seta voltar + título | `MainBottomBar(selected = 2)` |
| `ProfileScreen` | sim | seta voltar + título | `MainBottomBar(selected = 3)` |
| `LoginScreen` / `RegisterScreen` | sim | — | `AuthBottomBar` (Entrar / Cadastro) |
| `CheckoutScreen` / `AddPaymentMethodScreen` / `ProductDetailScreen` | sim | seta voltar + título | — |

Os componentes de barra ficam isolados em `core/ui/BottomNavBars.kt` (`MainBottomBar`, `AuthBottomBar`), o que evita redefinir layout em cada tela.

## 2. Componentes de navegação

- **`NavHost`** — host único declarado em `MainActivity.kt:38`.
- **`NavController`** (`rememberNavController()` em `MainActivity.kt:36`) — fonte da verdade do back stack, criado uma vez no nível mais alto.
- **`composable(route = …)`** — registra cada destino. Usado tanto para rotas estáticas (`"login"`, `"marketplace"`) quanto parametrizadas (`"product-detail/{id}"`).
- **`NavigationBar` + `NavigationBarItem`** (Material 3) — em `MainBottomBar` e `AuthBottomBar` para alternância entre seções principais.
- **`TopAppBar`** (Material 3) — barra superior reutilizada em quase todas as telas internas.
- **`SnackbarHost`** — feedback transitório (sucesso/erro) acoplado ao Scaffold.
- **`AlertDialog`** e **`ModalBottomSheet`** — usados para diálogos (confirmação de pedido, avaliação de itens) e bottom sheets (lista de reviews) **em vez de criar uma rota nova** para cada janela. Regra: rota só para tela inteira; diálogo é estado da tela atual.

## 3. Gerenciamento de rotas

### Rotas registradas

| Rota | Composable | De onde se chega |
|---|---|---|
| `login` (start) | `LoginScreen` | Inicial / após logout |
| `register` | `RegisterScreen` | Aba "Cadastro" da `AuthBottomBar` |
| `marketplace` | `MarketplaceScreen` | Sucesso do login (substitui `login` no back stack) |
| `product-detail/{id}` | `ProductDetailScreen` | Toque em `ProductCard` no Marketplace |
| `checkout` | `CheckoutScreen` | Ícone do carrinho na top bar do Marketplace |
| `add-payment-method` | `AddPaymentMethodScreen` | Botão "Outro método" no Checkout |
| `edit-payment-method/{id}` | `AddPaymentMethodScreen` (modo edição) | Toque em método existente no Checkout |
| `orders` | `OrdersScreen` | Aba "Meus Pedidos" da `MainBottomBar` |
| `profile` | `ProfileScreen` | Ícone de perfil ou aba "Perfil" da `MainBottomBar` |
| `support` | `SupportScreen` | Aba "Suporte" da `MainBottomBar` |

### Mapa de transições

```
                    ┌──────────┐
                    │  login   │◄────────┐
                    └────┬─────┘         │ aba "Entrar"
                         │ JWT ok        │
                         │ popUpTo login │
                         ▼               │
                    ┌──────────┐    ┌────┴─────┐
              ┌────►│marketplace│◄──┤ register │
              │     └────┬─────┘    └──────────┘
              │          │
              │   ┌──────┼─────────────┐
              │   ▼      ▼             ▼
              │ product checkout    support
              │ -detail   │
              │           │ "Outro método"
              │           ▼
              │   ┌─────────────────────┐
              │   │ add/edit-payment-   │
              │   │       method        │
              │   └─────────────────────┘
              │
              │     ┌──────────┐
              ├─────│  orders  │
              │     └──────────┘
              │     ┌──────────┐
              └─────│ profile  │  ← logout: popUpTo(0) → login
                    └──────────┘
```

### Padrões e diretrizes

- **Back stack controlado**: `popBackStack()` em todas as telas com seta voltar. Login → Marketplace usa `popUpTo("login") { inclusive = true }`, então o botão "voltar" do sistema sai do app em vez de retornar ao login.
- **Logout total**: `popUpTo(0) { inclusive = true }` em `MainActivity.kt:77` zera o back stack inteiro antes de empurrar `login` — impede voltar para uma tela autenticada após sair.
- **Re-entrada no Marketplace**: ao vir de outra aba, navegamos para `marketplace` com `popUpTo("marketplace") { inclusive = true }` para evitar empilhar instâncias duplicadas.
- **Argumentos tipados**: rotas com parâmetro usam `navArgument("id") { type = NavType.IntType }` (em `product-detail/{id}`) ou `NavType.StringType` (em `edit-payment-method/{id}`). String crua como argumento é proibido — sempre tipar via `NavType`.
- **Sem deep links** ainda — espaço reservado para uma fase futura (ex.: notificação push abrir um pedido específico).
- **Telas só recebem callbacks** (`onBack`, `onOpenCart`, `onOpenProductDetail`). A `MainActivity` é o único lugar que conhece o `NavController`. Isso mantém as telas testáveis e isoladas do `navigation-compose`.

## 4. Troca de dados entre telas

A troca de dados segue três padrões, escolhidos conforme o tipo do dado:

### a) Argumentos da rota (dados pequenos e estáveis)
Para identificadores: `nav.navigate("product-detail/$id")` em `MainActivity.kt:60`. O destino lê via `entry.arguments?.getInt("id")` (`MainActivity.kt:119`). Usado para: id de produto, id de método de pagamento em edição. **Não passamos objetos inteiros pela rota** — só o id.

### b) ViewModel compartilhado (estado que sobrevive à navegação)
O `MarketplaceViewModel` é instanciado **uma vez** em `MainActivity.kt:37` e injetado tanto no `MarketplaceScreen` quanto no `ProductDetailScreen` (`MainActivity.kt:124`). Isso garante que:
- Adicionar item ao carrinho a partir do detail atualiza o badge do carrinho na top bar do Marketplace ao voltar.
- A lista de produtos não é recarregada ao retornar do detail.

Cada feature isolada (auth, profile, orders, support) tem seu próprio ViewModel local, criado por `viewModel()` dentro do composable de tela, que carrega dados via repository → API.

### c) Backend como fonte da verdade (dados compartilhados entre fluxos)
Carrinho, endereços, métodos de pagamento, perfil e pedidos vivem no backend. As telas chamam o repository correspondente e re-renderizam com o estado fresco. Isso evita o anti-pattern de "passar o carrinho inteiro pela rota" — o `CheckoutScreen` busca seu próprio carrinho via `GET /api/cart` em vez de receber a lista do `MarketplaceScreen`.

Persistência local (`PaymentMethodLocalStore` com SharedPreferences + JSON) é usada **só** para preferências do dispositivo (método de pagamento padrão), nunca para dados que precisam estar sincronizados entre dispositivos.

### Por que esta arquitetura favorece a UX

1. **Voltar nunca perde estado relevante** — o ViewModel compartilhado e o backend como fonte da verdade fazem com que o usuário volte exatamente para onde estava.
2. **Login e logout têm fronteira clara** — `popUpTo` evita "voltar acidentalmente para uma tela logada após logout" ou "voltar para o login após autenticar".
3. **Bottom bar consistente** — as 4 abas principais (`Loja / Pedidos / Suporte / Perfil`) ficam estáveis em todas as telas internas que as usam, e a aba selecionada reflete a tela ativa.
4. **Navegação tipada** — argumentos via `NavType` falham em compilação se o tipo divergir, em vez de quebrar em runtime.
5. **Sem rota para diálogo** — diálogos e bottom sheets são estado da tela atual, não destinos. Isso simplifica o back stack e mantém a navegação previsível.

## Pontos de atenção / próximos passos

- **Auth gating**: quando entrar persistência real de sessão, criar um `nav graph` aninhado para fluxo `auth/` e outro para `app/`, com redirect baseado em estado de sessão (em vez de `popUpTo` espalhado).
- **Deep links** para notificações (ex.: `edu://order/{id}`).
- **Animações de transição** customizadas (hoje usamos as default do Navigation Compose).
