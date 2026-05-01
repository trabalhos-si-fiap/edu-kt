# Documentação — Edu (Kotlin/Compose)

Centralizador da documentação técnica do app. Para instalação e build, ver o [README principal](../README.md).

## Sumário

| Seção | Descrição |
|---|---|
| [Arquitetura](architecture.md) | Estrutura feature-first, camadas, convenções de código |
| [Design System](design-system.md) | Cores, tipografia, formas, componentes reutilizáveis |
| [Telas](screens.md) | Descrição funcional de cada tela e seu estado |
| [Navegação](navigation.md) | Rotas, mapa de transições, deep links |
| [Persistência local](persistence.md) | O que faz sentido salvar no dispositivo e como |
| [Integração com back-end](backend-integration.md) | Fronteira device × servidor (Spring Boot) |
| [Hot reload e dev workflow](dev-workflow.md) | Live Edit, Compose Preview, Makefile, testes unitários |
| [Créditos](credits.md) | Equipe e ano |

## Convenções deste diretório

- Cada arquivo cobre **um único assunto**. Se o tema crescer, divida em sub-arquivos.
- Links internos sempre relativos (`./outro-arquivo.md`).
- Diagramas em ASCII ou em Mermaid para serem versionáveis.
- Decisões arquiteturais relevantes ficam aqui, não em comentários no código.
