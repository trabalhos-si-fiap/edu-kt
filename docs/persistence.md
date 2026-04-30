# Persistência local

Hoje o app é 100% mockado em memória. Quando entrar persistência, este é o plano recomendado para o escopo do **marketplace**.

## O que persistir

| Dado | Mecanismo sugerido | Justificativa |
|---|---|---|
| **Carrinho** | DataStore (JSON serializado) | Item adicionado precisa sobreviver a fechar o app. |
| **Métodos de pagamento salvos** | DataStore + EncryptedSharedPreferences | O formulário de "Adicionar Método" hoje só mostra um snackbar — persistir torna o checkbox "Definir como padrão" útil. Apenas últimos 4 dígitos + bandeira; CVV nunca persiste. |
| **Método padrão** (id) | DataStore Preferences | Pré-seleciona a opção no Checkout. |
| **Histórico de pedidos** | DataStore (JSON) | Salvar ao finalizar checkout para alimentar a `OrdersScreen` em vez do mock fixo. |

## O que **não** persistir localmente

- Catálogo de produtos — fonte da verdade é o backend; cache só se o app exigir uso offline.
- Estado de UI (scroll, aba, expand/collapse) — `rememberSaveable` resolve, e mesmo isso é descartável.
- Senhas em claro (sequer no formulário concluído).
- Dados completos de cartão (número cheio, CVV, validade plena). PCI-DSS proíbe persistir CVV mesmo encriptado.

## Stack proposta

- **Jetpack DataStore** (`androidx.datastore:datastore-preferences` para flags simples; `datastore` puro com Proto/JSON para listas).
- **Kotlinx Serialization** para serializar carrinho e pedidos.
- **`androidx.security:security-crypto`** se for armazenar qualquer coisa sensível (token, últimos-4 + bandeira).
- **Não** usar Room — overkill para listas pequenas de leitura/escrita inteira. Reavaliar se entrar busca/filtro local ou catálogo offline.

## Estrutura

```
features/marketplace/data/
├── local/
│   ├── CartDataSource.kt          # ler/escrever carrinho no DataStore
│   ├── PaymentMethodsDataSource.kt
│   └── OrderHistoryDataSource.kt
└── repository/
    └── MarketplaceRepository.kt   # combina local + (futuro) remoto
```

`ViewModel` na camada `presentation` consome o `Repository`; nunca o DataStore direto.

## Migração / versionamento

Adotar um campo `version` no JSON desde o início. Mudanças quebradoras → escrever migration que lê a versão antiga e converte.
