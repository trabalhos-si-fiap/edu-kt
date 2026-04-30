# Integração com back-end (Spring Boot)

Diretrizes para quando o back-end Java/Spring Boot entrar. Este doc define **a fronteira** — o que pertence a cada lado — e padrões de comunicação. Detalhes de endpoints virão num `api.md` separado quando o contrato existir.

## Fica só no celular

| Dado | Por quê |
|---|---|
| Estado de UI (scroll, aba, expanded) | Não tem valor de negócio; `rememberSaveable` cobre. |
| Tokens de sessão (JWT, refresh) | Em `EncryptedSharedPreferences`. Servidor revoga via expiração/blacklist; não rastreia onde está guardado. |
| FCM push token / device id | Gerados pelo dispositivo. Você *envia* ao servidor para receber push, mas o dono é o cliente. |
| Preferências de UI (tema, fonte) | Por dispositivo; viram conta-bound só se o produto pedir multi-device coerente. |
| Rascunhos de formulário | Cadastro pela metade, "Adicionar Método" antes de salvar. Sumir num crash é UX ruim, mas não vale endpoint. |
| Cache de dados do servidor | Catálogo, histórico. Fonte da verdade é o backend; local só melhora perceived performance e habilita offline. |
| Histórico de busca / "visto recentemente" | Pessoal e barato; só vai pro servidor se o produto vender "continue de onde parou em outro dispositivo". |
| Fila de analytics | Eventos bufferizados localmente até a próxima conexão. |

## Vai obrigatoriamente para o back-end

- Catálogo de produtos e estoque.
- Pedidos e seu ciclo de vida (criação, status, eventos de logística).
- Métodos de pagamento salvos — guardar o **token do gateway** + últimos 4 dígitos. Cartão cru *nunca* passa pelo seu servidor.
- Perfil de usuário (nome, e-mail, telefone, data de nascimento).
- Autenticação — login, registro, recovery, refresh.

## Borderline: carrinho

Três modelos possíveis. Recomendado: **híbrido**.

| Modelo | Prós | Contras |
|---|---|---|
| Só local | Simples, offline funciona, não exige login | Some ao reinstalar/trocar de aparelho |
| Só servidor | Multi-device, histórico de abandono | Requer auth para adicionar item; sem rede = sem carrinho |
| **Híbrido** (recomendado) | Local é fonte da verdade enquanto logado; sync em background via `WorkManager` | Precisa lógica de merge (last-write-wins por SKU) |

## Stack de comunicação

- **Retrofit + OkHttp + Kotlinx Serialization** — padrão mais comum, baixa fricção com Spring (JSON).
- **Auth interceptor** anexa o JWT em todas as chamadas autenticadas.
- **Refresh interceptor** intercepta `401`, faz refresh e re-tenta uma vez (idempotente). Se o refresh falhar, derruba sessão e navega para `login`.
- **Coroutines + Flow** — `suspend fun` no service, `Flow` quando há mais de uma emissão (status de pedido, push de novidade no carrinho).
- **DTO ↔ domínio**: nunca expor DTO Retrofit direto à `presentation`. Mapper na `data/`.

## Padrões de erro

- Backend devolve `{ code, message, details? }` em corpo de erro.
- App mapeia para um `sealed class AppError` com casos cobertos no UI (rede, validação, autenticação, servidor).
- Snackbar para erros de operação; banner persistente para "sem rede".

## Segurança

- HTTPS obrigatório (cleartext bloqueado no `network-security-config.xml`).
- Certificate pinning quando o produto justificar.
- Tokens em `EncryptedSharedPreferences`. Não logar tokens, CPF, números de cartão — nem em `debug`.
- Toda chamada de mutação (POST/PUT/DELETE) precisa de JWT válido; o servidor é a única autoridade.

## Ordem de implementação sugerida

1. Auth (login + cadastro real, JWT, refresh).
2. Catálogo (substitui mocks do Marketplace).
3. Carrinho híbrido.
4. Checkout + pagamento (gateway tokenizado).
5. Pedidos com push de status (FCM + endpoint REST).
