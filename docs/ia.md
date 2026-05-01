# IA conversacional e IA generativa no Edu

Reflexão crítica sobre o uso de inteligência artificial no app, articulando os dois eixos estudados na Fase 3: **chatbots com IBM Watson Assistant** e **IA generativa / LLMs**. Esta análise embasa a decisão de **não embarcar IA na entrega da Fase 3** e de **planejar uma adoção híbrida e escopada para uma fase futura**, sob requisitos de segurança e ética compatíveis com um app que lida com dados de estudantes.

## 1. Há espaço para um assistente conversacional no projeto?

**Sim, há espaço — mas só em pontos específicos da jornada.** O app já tem uma aba **Suporte** (`SupportScreen`) cuja proposta é abrir um canal direto entre aluno e mentor. Hoje, o backend (`apps/support/services.py`) responde com uma mensagem automática fixa ("Recebemos sua mensagem. Um mentor responderá em breve…"). Esse é o ponto natural onde um assistente entraria, com dois objetivos:

1. **Triagem e auto-resolução** de dúvidas operacionais frequentes (status de pedido, segunda via de boleto, recuperação de senha, política de reembolso).
2. **Apoio pedagógico** sobre conteúdo do material adquirido (resumir um capítulo, gerar exercícios, explicar um conceito).

Já o restante do app — login, catálogo, checkout, perfil — **não se beneficia** de um assistente. Inserir IA nessas telas seria solução à procura de problema: a UX de marketplace já está resolvida com componentes nativos (busca, filtro, lista, detalhe).

## 2. Chatbot tradicional (Watson) ou IA generativa?

A escolha não é binária. Cada tipo de tarefa tem o "tamanho" certo:

| Tarefa | Melhor abordagem | Por quê |
|---|---|---|
| "Onde está meu pedido?", "Como troco a senha?", "Quais formas de pagamento?" | **Watson Assistant (chatbot tradicional, fluxos definidos)** | Intenção fechada, resposta determinística, integração direta com a API interna (`GET /api/orders/{id}`). Auditável, previsível e barato. |
| "Resuma esse capítulo do material X", "Gere 5 questões sobre cinemática nível ENEM" | **LLM (IA generativa)** | Não há como pré-escrever fluxos para conteúdo aberto. Exige geração condicionada ao material e ao perfil do aluno. |
| "Estou com dificuldade em derivadas, por onde começo?" | **Híbrido** | Watson abre um fluxo guiado (diagnóstico de nível) e, ao chegar na resposta, delega para um LLM com contexto recuperado por RAG sobre o catálogo do aluno. |

**Conclusão:** Watson Assistant cobre **suporte operacional**; LLM cobre **apoio pedagógico**. Misturar os dois evita o erro comum de jogar uma intenção transacional ("cadê meu pedido?") em um LLM — caro, lento, alucinável e desnecessário.

## 3. Quais tarefas seriam apoiadas

- **FAQ automatizado** (Watson): top-N dúvidas extraídas dos tickets reais.
- **Status de pedido** (Watson + chamada à API interna): "Onde está #EDU-000123?"
- **Recuperação assistida** (Watson): senha, e-mail de cadastro, segunda via.
- **Resumo de material** (LLM com RAG sobre o conteúdo do produto comprado).
- **Geração de questões** (LLM, condicionada ao material e ao nível do aluno).
- **Plano de estudo personalizado** (LLM): a partir do histórico de pedidos e do tempo disponível, sugerir uma trilha.
- **Esclarecimento de conceitos** (LLM com RAG): apenas usando como contexto material legítimo do catálogo, não a internet aberta.

## 4. Benefícios esperados para o usuário

- **Tempo de resposta**: respostas instantâneas para dúvidas comuns, sem esperar mentor humano.
- **Estudo ativo, não passivo**: gerar exercícios e resumos transforma material estático em ferramenta de prática.
- **Continuidade fora do horário comercial**: triagem 24/7 sem custo marginal por mensagem.
- **Personalização**: trilhas e exercícios adaptados ao histórico real do aluno (dados que o app já possui).
- **Liberação do mentor humano** para o que importa: dúvidas complexas e acompanhamento longitudinal, não copiar-e-colar política de reembolso.

## 5. Riscos, limitações e cuidados éticos

Esta é a parte que **bloqueia a entrega na Fase 3**. Antes de embarcar IA, é preciso resolver:

### Risco — Alucinação em contexto educacional
LLMs inventam fatos. Em educação, uma resposta errada com tom seguro é pior do que nenhuma resposta. **Mitigação**: RAG estrito sobre o material licenciado, com citação explícita ("segundo o capítulo 3 do guia X, página Y"); recusa explícita quando não houver evidência ("não encontrei isso no seu material"); proibido completar a partir de conhecimento paramétrico.

### Risco — Vazamento de dados sensíveis (LGPD)
O app armazena nome, e-mail, telefone, data de nascimento, endereços e histórico de compra de menores de idade. Mandar isso para um provedor externo de LLM viola minimização de dados. **Mitigação**: PII removida antes de qualquer chamada externa (substituir por placeholders); contratos de DPA com o provedor; logs sem PII; preferência por modelos com opção de não-treinamento sobre os dados do cliente.

### Risco — Comparação de tokens e segredos
A integração com Watson/LLM exige API keys. Já é regra do projeto (`CLAUDE.md` §5, §9): segredos só em variáveis de ambiente, comparações em tempo constante (`hmac.compare_digest`). **Mitigação**: chaves no `.env`, rotacionadas; nunca logadas.

### Risco — Custo e latência descontrolados
LLMs cobram por token e respondem em segundos. Sem teto, um usuário malicioso pode esgotar o orçamento. **Mitigação**: rate limiting por usuário com `cache.add()` / `cache.incr()` (regra §10 do `CLAUDE.md`); limite de tokens por requisição; circuit breaker se o gasto diário ultrapassar X.

### Risco — Prompt injection
Material do catálogo pode conter instruções escondidas para o LLM ("ignore as instruções anteriores e…"). **Mitigação**: separar canal de instrução do canal de conteúdo; sanitizar; nunca executar ações privilegiadas a partir de output do LLM (ex.: nunca o LLM dispara um POST de reembolso).

### Risco — Viés e equidade
LLMs reproduzem vieses dos dados de treino. Em educação, isso pode penalizar alunos por sotaque, vocabulário ou contexto socioeconômico. **Mitigação**: avaliação periódica com casos de teste diversos; opção de pedir mentor humano sempre visível.

### Risco — Dependência cognitiva
Se o aluno usa o LLM para fazer todo exercício, o app vira muleta, não ferramenta de estudo. **Mitigação**: priorizar geração de exercícios e correção comentada sobre "dar a resposta pronta"; UX que reforce tentativa antes de revelar a solução.

### Limitação técnica — Watson + Granian + Django
A integração via webhook requer endpoint público. Em desenvolvimento local, isso exige túnel (ngrok/cloudflared). Plano: começar em homologação com um único skill em Watson, mockando webhooks em testes de integração com `httpx`.

### Limitação técnica — RAG exige infraestrutura
Vector store, embeddings, ingestão dos materiais — infraestrutura nova que não existe no projeto. Adotar de forma incremental: começar com FAQ (sem RAG) → suporte com retrieval simples por palavra-chave → embeddings só quando o volume justificar.

## 6. Decisão para a Fase 3

**Não embarcar IA nesta fase.** A entrega cobre marketplace, checkout, perfil, pedidos e suporte com mentor humano simulado. Os riscos acima — em especial LGPD com dados de estudantes e custo de RAG — exigem trabalho de infraestrutura e segurança que está fora do escopo da Fase 3.

**Plano para fase futura:**
1. Substituir a auto-resposta hardcoded em `apps/support/services.py` por um fluxo Watson Assistant cobrindo as 10 dúvidas mais frequentes.
2. Em sequência, prototipar geração de questões com LLM sobre **um único** material do catálogo (escopo controlado), com PII removida e rate limit por usuário.
3. Avaliar métricas (taxa de resolução, satisfação, custo) antes de expandir.

Esta decisão não nega o valor da IA — afirma que **adicionar IA mal feita é pior do que não ter IA**, especialmente em um app educacional onde a expectativa de correção é alta e o público é parcialmente menor de idade.
