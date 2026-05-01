# Wireframes e protótipo

Esta página documenta a abordagem de prototipação do Edu para a entrega da Fase 3.

## Decisão: o app **é** o protótipo

Em vez de produzir wireframes em ferramenta externa (Figma, Miro, Pencil) e depois reimplementar em código, a equipe optou por **construir o protótipo já em Kotlin + Jetpack Compose**, usando o próprio app como artefato de design. Isso é viável porque:

- Compose permite iterar visualmente em tempo real via **Compose Preview** e Live Edit (ver [dev-workflow.md](dev-workflow.md)).
- Os tokens de design (cores, tipografia, raios, espaçamentos) já estão centralizados em `app/src/main/java/br/com/edu/core/theme/` e em [design-system.md](design-system.md), o que dá ao protótipo a mesma coerência visual de um wireframe de alta fidelidade.
- Reaproveita o esforço de implementação — o protótipo evolui para o produto, em vez de virar um arquivo `.fig` esquecido.

O resultado é um **protótipo de alta fidelidade, navegável e funcional**, com integração real ao backend, em vez de telas estáticas em uma ferramenta de mockup.

## Princípios de UX/UI aplicados

### Hierarquia de informação
- **Título → busca → destaque → grid de produtos** no Marketplace: o usuário lê de cima para baixo seguindo a importância.
- **Pedidos** com header (#id + data + status), conteúdo (carrossel de itens) e ações (rodapé) — três camadas claras por card.
- **Checkout** estruturado em três blocos numerados visualmente: revisão do carrinho → endereço → pagamento.

### Coerência com a navegação
- A `MainBottomBar` repete as quatro mesmas abas em todas as telas internas (`Loja / Pedidos / Suporte / Perfil`), com a aba ativa destacada — o usuário sempre sabe onde está.
- TopAppBar com seta voltar consistente em telas de detalhe (Checkout, Add Payment, Profile, Product Detail).

### Affordances visíveis
- Botão primário roxo (`Primary` `#1A1A2E` ou `Purple` `#5B00DF`) é sempre ação de fechamento de fluxo (Entrar, Confirmar, Salvar).
- Cards selecionáveis (endereço, método de pagamento) ganham borda roxa 2dp + check-badge quando ativos.
- Inputs com erro mostram texto vermelho abaixo, sem mover o layout.

### Feedback imediato
- `Snackbar` para confirmação ("Cadastro realizado!", "Método salvo") e erro ("Credenciais inválidas").
- Spinner em telas com fetch (Marketplace, Orders, Profile).
- Botão "Tentar novamente" em estado de erro de rede, em vez de apenas mensagem de falha.

### Redução de fricção
- "Comprar novamente" em um toque no `OrdersScreen`.
- Avaliação de todos os itens de um pedido em um único `AlertDialog` com `StarPicker`.
- Endereço favorito pré-selecionado no Checkout.
- Formatação automática de cartão, telefone e data via `VisualTransformation`.

### Acessibilidade básica
- Tipografia Lexend Deca (escolhida por legibilidade, especialmente em quem tem dislexia).
- Contraste alto em texto sobre fundo claro.
- Áreas de toque ≥ 48dp em botões (padrão do Material 3).
- Ícones acompanhados de texto sempre que possível.

## Telas principais (mapa visual)

Para a descrição funcional de cada tela, ver [screens.md](screens.md). Para o fluxo entre elas, ver [navigation.md](navigation.md). Para os tokens visuais, ver [design-system.md](design-system.md).

| Tela | Função | Principais elementos UX |
|---|---|---|
| **Login** | Autenticação | Card central, divisor "Ou entre com", botões sociais visuais, créditos no rodapé |
| **Cadastro** | Criação de conta | 6 campos com validação local, máscara em telefone, DatePicker em nascimento |
| **Marketplace** | Descoberta de produtos | Search + featured card + grid de cards com rating; bottom sheet de reviews |
| **Product Detail** | Decisão de compra | Imagem grande, descrição, CTA "Adicionar ao carrinho", reviews |
| **Checkout** | Finalização do pedido | 3 blocos sequenciais (carrinho → endereço → pagamento) + diálogo de confirmação |
| **Add Payment Method** | Cadastro de meio de pagamento | Toggle de tipo (Cartão / PIX / Boleto), campos formatados em tempo real |
| **Orders** | Histórico e recompra | Card por pedido com carrossel de itens, "Comprar novamente", "Avaliar itens" |
| **Profile** | Dados pessoais | Avatar com inicial, edição inline, atalhos para outras seções |
| **Support** | Canal com mentor | Lista de mensagens + input — placeholder para futura integração com Watson/LLM (ver [ia.md](ia.md)) |

## Ferramentas auxiliares usadas

- **Compose Preview** (`@Preview`) — iteração visual rápida sem rodar o app.
- **Android Studio Layout Inspector** — verificação do layout em runtime no emulador.
- **Material Theme Builder** (referência) — para validar contraste e harmonia da paleta roxa.

## Referência para o avaliador

Para visualizar os "wireframes" deste projeto, basta rodar o app em um emulador ou device físico. Em ~5 minutos é possível percorrer todas as 9 telas:

```bash
make dev    # sobe backend + emulador + instala/abre o app
```

Detalhes em [README principal](../README.md#quick-start-clonou-agora) e em [dev-workflow.md](dev-workflow.md).
