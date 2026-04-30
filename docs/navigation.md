# Navegação

Implementada com [`androidx.navigation:navigation-compose`](https://developer.android.com/jetpack/compose/navigation). O `NavHost` é montado em `MainActivity.kt::EduApp`.

## Rotas registradas

| Rota | Composable | De onde se chega |
|---|---|---|
| `login` (start) | `LoginScreen` | Inicial / após logout |
| `register` | `RegisterScreen` | Aba "Cadastro" da `AuthBottomBar` |
| `marketplace` | `MarketplaceScreen` | Sucesso do login (substitui `login` no back stack) |
| `checkout` | `CheckoutScreen` | Ícone do carrinho na top bar do Marketplace |
| `add-payment-method` | `AddPaymentMethodScreen` | Botão "Outro método" no Checkout |
| `orders` | `OrdersScreen` | Aba "Meus Pedidos" da `MainBottomBar` ou ícone de notificações |

## Mapa de transições

```
                    ┌──────────┐
                    │  login   │◄────────┐
                    └────┬─────┘         │ aba "Entrar"
                         │ teste/teste   │
                         │ popUpTo login │
                         ▼               │
                    ┌──────────┐    ┌────┴─────┐
              ┌────►│marketplace│◄──┤ register │
              │     └────┬─────┘    └──────────┘
              │ "Meus    │ ícone carrinho
              │ Pedidos" ▼
              │     ┌──────────┐
              │     │ checkout │
              │     └────┬─────┘
              │          │ "Outro método"
              │          ▼
              │   ┌─────────────────────┐
              │   │ add-payment-method  │
              │   └─────────────────────┘
              │
              │     ┌──────────┐
              └─────│  orders  │
                    └──────────┘
```

## Padrões em uso

- **Back stack**: `popBackStack()` em todas as telas com TopAppBar (seta voltar). Login → Marketplace usa `popUpTo("login") { inclusive = true }`, então pressionar o botão "voltar" do sistema sai do app em vez de voltar para o login.
- **Argumentos**: nenhuma rota recebe argumento ainda. Quando entrar (ex.: `order/{id}`), preferir argumentos tipados via `NavType` em vez de strings cruas.
- **Deep links**: não configurados.

## Pontos de atenção

- **Não criar rota nova só para um diálogo** — Compose já tem `Dialog`/`AlertDialog`/`ModalBottomSheet`. Use rotas para telas inteiras.
- **Cada composable de tela recebe apenas callbacks** (`onBack`, `onOpenCart`, …). A `MainActivity` é o único lugar que conhece o `NavController`. Isso mantém as telas testáveis e isoladas do `navigation-compose`.
- Quando entrar autenticação real, criar um `nav graph` aninhado para fluxo `auth/` e outro para `app/`, e usar um redirect baseado em estado de sessão (sem deixar o `popUpTo` espalhado).
