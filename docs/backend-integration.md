# Integração com back-end (Django + django-ninja)

Estado atual da integração entre o app Android e o backend Django, e as diretrizes para evoluí-la. O backend vive em `back-end/` e expõe a API em `/api/` via [django-ninja](https://django-ninja.dev/).

> Histórico: a primeira versão deste doc previa Spring Boot. O backend real do projeto é Django; manteve-se Retrofit/OkHttp do lado do app.

## O que já está integrado

| Feature | Endpoint | Status |
|---|---|---|
| Login | `POST /api/auth/login` | ✅ Tela `LoginScreen` chama o backend, salva token, navega ao Marketplace |
| Cadastro | `POST /api/auth/register` | ✅ Tela `RegisterScreen` valida localmente e cria conta no backend; persiste `name`/`phone`/`birth_date` (nome é dividido em `first_name` + `last_name`) |
| Perfil | `GET /api/auth/me`, `PATCH /api/auth/me` | ✅ `ProfileScreen` carrega e edita nome/telefone/data de nascimento; ambas exigem `Authorization: Bearer <key>`. `birth_date` em ISO `YYYY-MM-DD` (string vazia limpa o campo); formato inválido retorna 400 |
| Catálogo | `GET /api/products` | ✅ `MarketplaceScreen` lista produtos reais com imagem (Coil), com busca server-side (`?q=`) e paginação (`?limit=&offset=`); resposta `{items, total, limit, offset}` inclui `rating_avg` (0–5) e `rating_count` por produto. Campo de busca debounce 300ms no `MarketplaceViewModel`. |
| Detalhe do produto | `GET /api/products/{id}` | ✅ `ProductDetailScreen` busca o produto por id direto no backend (não filtra a listagem paginada — antes falhava com "Produto não encontrado" para qualquer item além do limite default de 20). Resposta com `rating_avg`/`rating_count` agregados. |
| Avaliações | `GET /api/products/{id}/reviews` | ✅ `ReviewsBottomSheet` abre ao tocar nas estrelas do card; resposta `{items, total, rating_avg, rating_count}` com `author/rating/comment/created_at`. Paginação `?limit=&offset=`. |
| Carrinho | `/api/cart/*` | ✅ `CartViewModel` singleton sincroniza add/remove com o backend; `CheckoutScreen` lê do servidor |
| Pedidos | `POST /api/orders`, `GET /api/orders`, `POST /api/orders/{id}/rebuy` | ✅ `CheckoutScreen` cria o pedido (esvazia o carrinho server-side) e `OrdersScreen` lista. Cada item vem com `image_url`, `rating_avg`, `rating_count` para o carrossel. "Comprar novamente" chama `rebuy` (incrementa carrinho). |
| Avaliação pós-compra | `POST /api/products/{id}/reviews` | ✅ Diálogo "Avaliar itens" envia uma `Review` por item com nota 1–5; autor derivado do `user.get_full_name()` ou `email`. |

## Configuração de rede

- `BASE_URL` é definido em tempo de build via `buildConfigField` em `app/build.gradle.kts`.
  Por padrão aponta para o IP da LAN do PC do dev na porta `8000`. Trocar por flavor /
  `local.properties` quando houver mais de um ambiente.
- Como é HTTP (não HTTPS), o S23 / Android 9+ bloqueia cleartext por padrão.
  Liberado **apenas para o IP do backend** em `app/src/main/res/xml/network_security_config.xml`,
  referenciado pelo `AndroidManifest.xml` via `android:networkSecurityConfig`.
- Backend já roda em `0.0.0.0:8000` (`back-end/docker-compose.yml`) com `ALLOWED_HOSTS=["*"]`
  e `CORS_ALLOW_ALL_ORIGINS=True`. Em produção, trocar ambos por valores restritivos.

### Sincronizar IP do backend no app

O IP da LAN muda (Wi-Fi diferente, DHCP, hotspot). Para evitar editar `build.gradle.kts`
na mão, há um helper Python que detecta o IP atual e reescreve **dois arquivos** ao
mesmo tempo: o `BASE_URL` em `app/build.gradle.kts` e o `<domain>` em
`app/src/main/res/xml/network_security_config.xml` — sem este último, o Android bloqueia
a chamada com "CLEARTEXT communication not permitted".

```bash
make configure-ip                  # detecta o IP da LAN automaticamente
make configure-ip IP=192.168.0.10  # força um IP específico (override em caso de erro)
make configure-ip PORT=8001        # mantém o IP atual e troca a porta
make show-ip                       # mostra o BASE_URL atual sem alterar
```

Por baixo dos panos roda `python3 scripts/configure_ip.py`, que abre um socket UDP para
um endereço público e lê o IP local que o kernel usaria — sem enviar pacote algum. Se a
detecção pegar o IP errado (VPN, segunda interface, container), use `IP=...` para forçar.

### Fluxo completo (clone → app rodando)

```bash
make doctor   # confere JDK, Docker, porta 8000, AVD, IP atual
make dev      # setup + IP + docker compose up --wait + emulador + run
```

`make dev` orquestra tudo: grava `local.properties`, sincroniza IP, sobe o backend
via `docker compose up -d --wait` (bloqueia até o healthcheck `/api/products` passar),
sobe o emulador se necessário e instala/abre o app. Login seedado:
`admin@admin.local` / `admin`.

## Camada de rede (`core/network`)

```
core/
├── auth/
│   └── TokenStore.kt         # singleton em memória (StateFlow<String?>)
└── network/
    ├── ApiClient.kt          # Retrofit + OkHttp + kotlinx-serialization
    └── AuthInterceptor.kt    # injeta "Authorization: Bearer <key>"
```

- **Retrofit 2.11** + **OkHttp 4.12** + **kotlinx-serialization 1.7** + converter da JakeWharton.
- `Json { ignoreUnknownKeys = true; coerceInputValues = true }` — tolerante a campos novos
  no backend sem quebrar o app.
- `HttpLoggingInterceptor` em `BODY` quando `BuildConfig.DEBUG` (visível via `adb logcat | grep OkHttp`).
- `AuthInterceptor` lê o token do `TokenStore` e adiciona `Authorization: Bearer <key>`.
  O backend (`apps/accounts/auth.BearerAuth`) é tolerante a prefixo: aceita `Bearer <key>`,
  `Token <key>` ou só `<key>` — faz `header.split()[-1]` antes de buscar o token. Útil
  para curl manual e para não quebrar se um cliente legado mandar outro prefixo.

### Token store

`TokenStore` é um singleton **em memória** — perde o token ao matar o app.
Próximo passo é migrar para `androidx.datastore:datastore-preferences` (eventualmente
`EncryptedSharedPreferences` se o produto exigir).

## Padrão por feature

Estrutura espelhada em `features/<feature>/`:

```
features/auth/
├── data/
│   ├── AuthRepository.kt          # orquestra chamadas, mapeia erros, salva token
│   └── remote/
│       ├── AuthApi.kt             # interface Retrofit
│       └── AuthDtos.kt            # @Serializable LoginRequest / TokenResponse / ...
└── presentation/
    ├── LoginScreen.kt             # Compose
    ├── LoginViewModel.kt          # MutableStateFlow<LoginUiState>
    ├── RegisterScreen.kt
    └── RegisterViewModel.kt
```

```
features/profile/
├── data/
│   ├── UserRepository.kt          # GET/PATCH wrappers, DTO → domínio
│   └── remote/
│       ├── UserApi.kt             # GET /auth/me, PATCH /auth/me
│       └── UserDto.kt             # UserDto + UserPatchDto (campos nullable)
├── domain/
│   └── UserProfile.kt
└── presentation/
    ├── ProfileScreen.kt           # header + edição inline + atalhos + logout
    └── ProfileViewModel.kt        # Loading / Ready(isEditing, saving, saveError) / Error
```

```
features/marketplace/
├── data/
│   ├── MarketplaceRepository.kt   # DTO → domínio (produtos + reviews)
│   └── remote/
│       ├── ProductApi.kt          # GET /products, GET /products/{id}, GET/POST /products/{id}/reviews
│       └── ProductDto.kt          # ProductDto / ReviewDto / ReviewInDto + envelopes paginados
├── domain/
│   └── Product.kt                 # Product (com ratingAvg/ratingCount) + Review
└── presentation/
    ├── MarketplaceScreen.kt
    ├── RatingStars.kt             # 5 estrelas com meio-ponto, cor âmbar
    ├── ReviewsBottomSheet.kt      # ModalBottomSheet com lista de avaliações
    └── MarketplaceViewModel.kt    # state (Loading/Ready/Error) + reviews (Hidden/Loading/Ready/Error)
```

```
features/orders/
├── data/
│   ├── OrdersRepository.kt        # listOrders / placeOrder / rebuy
│   └── remote/
│       ├── OrderApi.kt            # GET /orders, POST /orders, POST /orders/{id}/rebuy
│       └── OrderDto.kt            # OrderDto + OrderItemDto (image_url, rating_avg, rating_count)
├── domain/
│   └── Order.kt                   # Order + OrderItem (com itemsCount derivado)
└── presentation/
    └── OrdersViewModel.kt         # state (Loading/Ready/Error) + action (RebuySuccess/ReviewsSubmitted/Error)
```

Tela `OrdersScreen` vive em `features/marketplace/presentation/` (junto com checkout) por conveniência de navegação.

Convenções:
- **DTOs ficam em `data/remote`**, marcados com `@Serializable`. Espelham o JSON cru do backend.
- **Modelos de domínio em `domain/`**, sem dependência de Retrofit/serialization.
- **Repositories em `data/`** convertem DTO ↔ domínio e centralizam tratamento de erro.
- **ViewModels** expõem `StateFlow<UiState>` com hierarquia `sealed` (Loading / Ready / Error)
  ou `data class` com flags (`loading / error / success`) para fluxos sem dados de retorno.
- Telas Compose usam `viewModel()` e `collectAsState()`. Eventos efêmeros (snackbar, navegação
  pós-sucesso) ficam em `LaunchedEffect(state.field)` + função `consumed*()` no VM para limpar.

## Padrão de erro

Hoje:
- Repository chama `Response<T>` do Retrofit. Em `!isSuccessful`, decodifica `{"detail": "..."}`
  (formato do django-ninja) em `ErrorResponse` e lança `IllegalStateException(detail)`.
- ViewModel envolve em `Result.runCatching` e expõe `error: String?`.

Quando a base de erros crescer:
- Trocar `String?` por `sealed class AppError { Network, Validation, Auth, Server }`.
- Adicionar **refresh interceptor** para `401` (re-autentica e re-tenta uma vez).
- Snackbar para erros de operação; banner persistente para "sem rede".

## Mapeamento de campos sensíveis

- `price` vem como **string** do django-ninja (serialização padrão de `Decimal`).
  O DTO recebe `String`; a tela formata via `core/ui/CurrencyFormat.kt::formatBRL`,
  que usa `NumberFormat.getCurrencyInstance(Locale("pt", "BR"))` (ex.: `R$ 49,90`).
  Use sempre esse helper para qualquer valor monetário na UI — não reimplementar formatação local.
- `image_url` vem como string (pode ser vazia). `ProductDto` mapeia via `@SerialName("image_url")`
  para `imageUrl` (default `""`). `MarketplaceScreen` renderiza com `SubcomposeAsyncImage` (Coil)
  e cai no ícone-placeholder original em loading/erro/url vazia.
- `rating_avg` (float, 0.0 quando sem avaliações) e `rating_count` (int) são agregados
  pelo backend via `Avg("reviews__rating")` / `Count("reviews")` em `ProductOut`.
  `RatingStars` arredonda para meio-ponto (`(rating*2).roundToInt()`); o card só
  abre o bottom sheet quando `ratingCount > 0`.
- `birth_date` é validado como ISO `YYYY-MM-DD` no backend (`date.fromisoformat`). Inválido →
  400 `{"detail": "Invalid birth_date format, expected YYYY-MM-DD"}`. No `PATCH /auth/me`,
  string vazia limpa o campo (vira `null`); ausência (`null` no JSON) deixa como está.
- `UserOut` lista campos explicitamente (`id`, `email`, `name`, `phone`, `birth_date`,
  `date_joined`) — `from_attributes` é evitado para não vazar campos sensíveis do `User`
  (regra de segurança #6 do `CLAUDE.md`).

## O que ainda fica só no celular

| Dado | Por quê |
|---|---|
| Estado de UI (scroll, expanded, aba) | Sem valor de negócio; `rememberSaveable` cobre |
| FCM push token / device id | Gerado pelo dispositivo, enviado ao servidor para receber push |
| Preferências (tema, fonte) | Por dispositivo; vira conta-bound só se houver multi-device |
| Rascunhos de formulário | UX local; não vale endpoint |
| Cache de catálogo | Backend é fonte da verdade; cache local melhora perceived performance |
| Histórico de busca | Pessoal e barato |

## Carrinho

**Servidor é a única fonte de verdade.** Cada ação (`addItem`, `decrementItem`, `removeAll`)
chama o backend e a resposta `CartOut` substitui o estado em memória do `CartViewModel`.
Sem cache local persistido — KISS, e evita merge de estados divergentes.

```
features/cart/
├── data/
│   ├── CartRepository.kt
│   └── remote/
│       ├── CartApi.kt              # GET /cart, POST /cart/items, DELETE /cart/items/{id}?quantity=
│       └── CartDto.kt              # CartDto / CartItemDto / CartItemInDto
├── domain/
│   └── Cart.kt                     # Cart + CartItem (totalQuantity helper)
└── presentation/
    └── CartViewModel.kt            # singleton (CartViewModel.get()), Mutex serializa mutações
```

- `CartViewModel.get()` é **singleton manual** (sem DI). Marketplace e Checkout compartilham
  a mesma instância pelo tempo de vida do processo, então o badge / total ficam consistentes
  sem passar nada pelo `NavHost`.
- `Mutex.withLock` em `runOp` serializa `add`/`remove` concorrentes — protege contra spam de
  toque sem precisar travar a UI por completo.
- `CheckoutScreen` chama `cartViewModel.load()` em `LaunchedEffect(Unit)` para refrescar ao
  abrir (cobre o caso de outro device ter alterado o carrinho).
- Botões da `CartItemCard`: **"Remover 1"** chama `decrementItem` (DELETE com `quantity=1`);
  **X** no canto chama `removeAll` (DELETE sem `quantity` — apaga o item inteiro).

Trade-off conhecido: sem rede, o carrinho não funciona. Aceitável enquanto o app exigir login
para qualquer ação útil. Cache local + sync com `WorkManager` é evolução futura se o produto
exigir uso offline.

## Segurança — pendências antes de prod

- [ ] HTTPS obrigatório; remover `cleartextTrafficPermitted`.
- [ ] `ALLOWED_HOSTS` específicos no Django; CORS restrito.
- [ ] Token persistido em `EncryptedSharedPreferences` ou `DataStore` com criptografia.
- [ ] Refresh token + interceptor de re-tentativa em `401`.
- [ ] Não logar tokens, CPF, dados sensíveis — checar `HttpLoggingInterceptor` desligado em release.
- [ ] Certificate pinning quando o produto justificar.

## Ordem de implementação restante

1. **Status de pedido no backend** — modelo atual de `Order` não tem `status`/etapa de entrega. Frontend trata todo pedido como entregue. Adicionar `status` (ex: `picking|transit|delivered`) + estimativa para reabilitar `ActiveOrderCard` com stepper.
2. **Tela de métodos de pagamento dedicada** — hoje o atalho do Perfil cai no `CheckoutScreen`; criar rota própria com listagem e edição.
3. **Persistência do token** — DataStore.
4. **Refresh / 401 handling**.
5. **Push (FCM)** — quando houver evento de status de pedido pelo backend.
6. **Restringir review por compra** — endpoint atual aceita review de qualquer produto autenticado; ideal exigir que o usuário tenha um `OrderItem` daquele produto.
