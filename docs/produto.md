# Produto — Edu

Documento de produto da entrega da Fase 3. Para a parte técnica, ver [architecture.md](architecture.md), [screens.md](screens.md) e [navigation.md](navigation.md).

## Visão geral

**Edu** é um app educacional Android que combina um **marketplace de materiais de estudo** (cursos, guias e materiais didáticos) com **suporte ao aluno** e **gestão de pedidos**. O backend é um BFF em Django + django-ninja que agrega os domínios `accounts`, `cart`, `catalog`, `orders` e `support` em uma API única consumida pelo cliente Kotlin.

A proposta é entregar uma experiência mobile leve, com identidade visual própria (paleta roxa, tipografia Lexend Deca, cards arredondados, gradiente claro de fundo) e fluxo curto entre descoberta → compra → suporte.

## Principais funcionalidades

| Eixo | Funcionalidade | Onde está |
|---|---|---|
| **Conta** | Cadastro com validação local (e-mail, senha forte, confirmação) e login via JWT | `RegisterScreen`, `LoginScreen` |
| | Edição inline de dados pessoais (nome, telefone com máscara, data de nascimento) | `ProfileScreen` |
| | Logout limpando token e back stack | `ProfileScreen` |
| **Catálogo** | Listagem em grid com busca, categoria em destaque e featured card | `MarketplaceScreen` |
| | Detalhe do produto com imagens, descrição e ação de carrinho | `ProductDetailScreen` |
| | Avaliações por produto (estrelas + comentários) com bottom sheet | `MarketplaceScreen` + `ReviewsBottomSheet` |
| **Carrinho / Pedido** | Carrinho persistente no backend, com badge de quantidade no topo | `MarketplaceScreen` (badge) + `CheckoutScreen` |
| | Checkout com seleção de endereço favorito e método de pagamento | `CheckoutScreen` |
| | Cadastro de método de pagamento (Cartão / PIX / Boleto) com formatação e validação local | `AddPaymentMethodScreen` |
| | Histórico de pedidos com carrossel de itens, "Comprar novamente" e avaliação por estrelas | `OrdersScreen` |
| **Suporte** | Canal de mensagens para mentor responder dúvidas | `SupportScreen` + `apps/support` (backend) |
| **Persistência local** | Métodos de pagamento gravados em `SharedPreferences` (JSON) | `PaymentMethodLocalStore` |

## Concorrentes no mercado

| Concorrente | O que oferece | Lacuna que abrimos espaço |
|---|---|---|
| **Udemy / Coursera / Alura** | Catálogo gigante de cursos online, conteúdo em vídeo, certificação | Foco em vídeo-aula e assinatura; pouca ênfase em material complementar tangível e em mentoria 1:1 |
| **Hotmart / Kiwify / Eduzz** | Marketplace de infoprodutos com checkout e split de pagamento | Centradas no produtor, não no aluno; UX de catálogo é fraca e sem trilha pedagógica |
| **Descomplica / Stoodi / Me Salva!** | Cursinhos online (vestibular/ENEM) com plano por assinatura | Verticalizados em pré-vestibular; não atendem outras jornadas (concursos, pós, idiomas) |
| **Amazon / Estante Virtual** | Compra de livros físicos/e-books | Marketplace genérico; sem curadoria educacional, sem mentoria, sem trilha |

## Diferenciais competitivos

1. **Marketplace verticalizado em educação, não em vídeo.** O foco não é "mais um catálogo de cursos em vídeo": é um shelf curado de **guias, materiais e cursos** complementares ao estudo formal, com avaliação social embutida no card.
2. **Mentoria/suporte como recurso de primeira classe.** O `SupportScreen` é uma das quatro abas principais do app — não fica escondido em um menu de "Ajuda". O canal direto com mentor é parte da proposta de valor, não um pós-venda.
3. **UX mobile-first com identidade própria.** Lexend Deca + paleta roxa + cards arredondados + gradiente claro entregam uma experiência coesa, em vez do visual genérico de marketplace. O design system é versionado em `docs/design-system.md`.
4. **"Comprar novamente" e re-avaliação inline no histórico.** O `OrdersScreen` permite recompor o carrinho a partir de um pedido anterior em um toque, e avaliar todos os itens em um único diálogo — fluxos que reduzem fricção em uso recorrente.
5. **Pagamento flexível com formatação client-side robusta.** Cartão / PIX / Boleto com formatação em tempo real (`VisualTransformation` para número de cartão e telefone), validação local antes de enviar e cobertura por testes unitários (`CardFormattingTest`).
6. **Backend BFF próprio.** Em vez de plugar um SaaS de marketplace, o app tem um backend Django enxuto sob nosso controle, o que permite evoluir regras (ex.: bundles, trilhas, descontos por curso completo) sem depender de roadmap de terceiro.

## Público-alvo

Estudantes de ensino médio, pré-vestibular e graduação que já consomem conteúdo educacional online e querem **complementar** seus estudos com material curado, com canal direto de dúvidas e histórico de compras organizado em um único app.

## Status atual da entrega (Fase 3)

- 9 telas implementadas e navegáveis (ver [screens.md](screens.md)).
- Backend Django funcional com migrations, seed de catálogo e seed de admin.
- Integração via Retrofit cobrindo auth, catálogo, carrinho, pedidos, perfil, endereços e suporte.
- Persistência local de métodos de pagamento (SharedPreferences).
- Identidade visual coerente (tokens em `core/theme/`).
- Créditos em `docs/credits.md` e no rodapé da tela de Login.

Próximos passos planejados: trilhas de estudo (sequenciamento de produtos), notificações de novos materiais e expansão do `SupportScreen` para histórico de tickets.
