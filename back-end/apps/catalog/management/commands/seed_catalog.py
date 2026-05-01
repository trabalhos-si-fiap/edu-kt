import hashlib
from decimal import Decimal

from django.core.management.base import BaseCommand

from apps.catalog.models import Product, Review

CATALOG: list[dict] = [
    {
        "name": "Planner ENEM 2026",
        "type": "Produto Digital",
        "subtype": "Planner de Estudos",
        "description": "Planner completo com organização semanal, metas e revisão espaçada para ENEM",
        "price": "29.90",
        "image_url": "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=800",
    },
    {
        "name": "Notion Template - Rotina de Estudos",
        "type": "Produto Digital",
        "subtype": "Template Notion",
        "description": "Template pronto no Notion com controle de matérias, tarefas e progresso",
        "price": "19.90",
        "image_url": "https://images.unsplash.com/photo-1517842645767-c639042777db?w=800",
    },
    {
        "name": "Guia Completo ENEM",
        "type": "Ebook",
        "subtype": "Guia de Preparação",
        "description": "Guia estratégico com técnicas de prova, gestão de tempo e redação",
        "price": "39.90",
        "image_url": "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
    },
    {
        "name": "Simulado preparatorio para ENEM",
        "type": "Avaliação",
        "subtype": "Simulado",
        "description": "Simulado completo com correção automática e análise de desempenho",
        "price": "14.90",
        "image_url": "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800",
    },
    {
        "name": "BioVest - Questões Biologia",
        "type": "Avaliação",
        "subtype": "Pacote de Questões",
        "description": "Banco com +300 questões de biologia focadas em vestibular",
        "price": "24.90",
        "image_url": "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=800",
    },
    {
        "name": "Redação Nota 1000",
        "type": "Curso",
        "subtype": "Redação",
        "description": "Curso com estrutura, repertório e exemplos de redações nota máxima",
        "price": "49.90",
        "image_url": "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800",
    },
    {
        "name": "Planner de Revisão Inteligente",
        "type": "Produto Digital",
        "subtype": "Planner de Estudos",
        "description": "Sistema baseado em repetição espaçada para maximizar memorização",
        "price": "27.90",
        "image_url": "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800",
    },
    {
        "name": "Notion Template - Controle de Simulados",
        "type": "Produto Digital",
        "subtype": "Template Notion",
        "description": "Template para acompanhar desempenho em simulados e evolução",
        "price": "17.90",
        "image_url": "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800",
    },
    {
        "name": "Física Essencial para Vestibular",
        "type": "Ebook",
        "subtype": "Guia da Matéria",
        "description": "Conteúdo direto ao ponto com fórmulas e exercícios resolvidos",
        "price": "34.90",
        "image_url": "https://images.unsplash.com/photo-1636466497217-26a8cbeaf0aa?w=800",
    },
    {
        "name": "Simulado preparatorio para Fuvest",
        "type": "Avaliação",
        "subtype": "Simulado",
        "description": "Simulado específico para Fuvest com nível avançado",
        "price": "16.90",
        "image_url": "https://images.unsplash.com/photo-1513258496099-48168024aec0?w=800",
    },
    {
        "name": "Matemática Básica para ENEM",
        "type": "Curso",
        "subtype": "Curso em Vídeo",
        "description": "Curso em vídeo com fundamentos e exercícios práticos",
        "price": "59.90",
        "image_url": "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800",
    },
    {
        "name": "Cronograma 90 dias ENEM",
        "type": "Produto Digital",
        "subtype": "Planner de Estudos",
        "description": "Plano intensivo de 90 dias com foco em revisão e prática",
        "price": "21.90",
        "image_url": "https://images.unsplash.com/photo-1506784365847-bbad939e9335?w=800",
    },
    {
        "name": "BioVest - Revisão Rápida",
        "type": "Ebook",
        "subtype": "Resumo da Matéria",
        "description": "Resumo estratégico de biologia para revisão pré-prova",
        "price": "18.90",
        "image_url": "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800",
    },
    {
        "name": "Pacote Simulados ENEM (5 provas)",
        "type": "Pacote",
        "subtype": "Pacote de Simulados",
        "description": "5 simulados completos com análise de desempenho",
        "price": "49.90",
        "image_url": "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800",
    },
    {
        "name": "Template Notion - Vida do Vestibulando",
        "type": "Produto Digital",
        "subtype": "Template Notion",
        "description": "Organização completa: estudos, hábitos, metas e saúde mental",
        "price": "24.90",
        "image_url": "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=800",
    },
]

_FREE_IMAGES = [
    "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=800",
    "https://images.unsplash.com/photo-1517842645767-c639042777db?w=800",
    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
    "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800",
    "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=800",
    "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800",
    "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800",
    "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800",
    "https://images.unsplash.com/photo-1636466497217-26a8cbeaf0aa?w=800",
    "https://images.unsplash.com/photo-1513258496099-48168024aec0?w=800",
    "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800",
    "https://images.unsplash.com/photo-1506784365847-bbad939e9335?w=800",
    "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800",
    "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800",
    "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=800",
]

_EXTRA_CATALOG: list[tuple[str, str, str, str, str]] = [
    ("Caderno de Resumos - Química Orgânica", "Ebook", "Resumo da Matéria", "Resumos visuais de funções orgânicas, isomeria e reações principais", "22.90"),
    ("Mapa Mental - Revolução Industrial", "Produto Digital", "Mapa Mental", "Mapa mental ilustrado cobrindo causas, fases e consequências", "9.90"),
    ("BioVest - Genética e Evolução", "Avaliação", "Pacote de Questões", "Banco com 200+ questões de genética mendeliana e evolução", "19.90"),
    ("Curso Intensivo - Geometria Plana", "Curso", "Curso em Vídeo", "Aulas em vídeo com demonstrações e exercícios resolvidos", "54.90"),
    ("Simulado Unicamp Fase 1", "Avaliação", "Simulado", "Simulado fiel ao estilo Unicamp com gabarito comentado", "16.90"),
    ("Pacote História do Brasil", "Pacote", "Pacote de Estudos", "Conjunto de 4 ebooks cobrindo do período colonial à Nova República", "59.90"),
    ("Notion Template - Cronograma de Concursos", "Produto Digital", "Template Notion", "Acompanhamento de editais, ciclos de estudo e revisões", "21.90"),
    ("Redação Argumentativa - Templates Prontos", "Ebook", "Guia de Redação", "Estruturas e conectivos para redação dissertativa-argumentativa", "17.90"),
    ("Curso Completo - Inglês para Vestibular", "Curso", "Curso em Vídeo", "Gramática, leitura e vocabulário focados em provas", "69.90"),
    ("Planner Mensal Vestibulando 2026", "Produto Digital", "Planner de Estudos", "Planner imprimível com metas mensais e revisão por matéria", "23.90"),
    ("Apostila Filosofia - Pré-Socráticos a Modernos", "Ebook", "Guia da Matéria", "Panorama dos principais pensadores e correntes filosóficas", "32.90"),
    ("Simulado ITA - Matemática Avançada", "Avaliação", "Simulado", "Simulado nível ITA com questões discursivas e gabarito", "19.90"),
    ("Banco de Questões - Português ENEM", "Avaliação", "Pacote de Questões", "+250 questões de interpretação, gramática e literatura", "24.90"),
    ("Guia Estratégico - Gestão de Tempo na Prova", "Ebook", "Guia de Preparação", "Técnicas para administrar relógio e priorizar questões", "15.90"),
    ("Curso Express - Trigonometria", "Curso", "Curso em Vídeo", "Curso direto ao ponto com fórmulas e aplicações", "39.90"),
    ("Mapa Mental - Sistemas do Corpo Humano", "Produto Digital", "Mapa Mental", "Mapas ilustrados de cada sistema fisiológico", "12.90"),
    ("Pacote Redação - 50 Temas Atuais", "Pacote", "Pacote de Redação", "50 propostas de redação com repertório e exemplos", "44.90"),
    ("Notion Template - Diário de Estudos", "Produto Digital", "Template Notion", "Diário com humor, foco e revisão pós-estudo", "16.90"),
    ("Resumo Visual - Geografia Política", "Ebook", "Resumo da Matéria", "Resumos infográficos de geopolítica e blocos econômicos", "18.90"),
    ("Simulado OAB 1ª Fase", "Avaliação", "Simulado", "Simulado completo no padrão FGV com gabarito comentado", "29.90"),
    ("Curso de Espanhol para Vestibular", "Curso", "Curso em Vídeo", "Foco em compreensão textual e falsos cognatos", "49.90"),
    ("Apostila Química Inorgânica", "Ebook", "Guia da Matéria", "Tabela periódica, ligações e funções inorgânicas", "27.90"),
    ("Planner Semanal Imprimível", "Produto Digital", "Planner de Estudos", "Planner em PDF para imprimir e organizar a semana", "11.90"),
    ("Banco de Questões - Matemática Discreta", "Avaliação", "Pacote de Questões", "Combinatória, probabilidade e lógica com gabarito", "22.90"),
    ("Cronograma 30 dias - Reta Final ENEM", "Produto Digital", "Planner de Estudos", "Plano intensivo com revisão diária e simulados rápidos", "19.90"),
    ("Guia Completo - Literatura Brasileira", "Ebook", "Guia da Matéria", "Escolas literárias, autores e obras cobradas em vestibulares", "34.90"),
    ("Simulado Fuvest 2ª Fase", "Avaliação", "Simulado", "Provas discursivas no estilo Fuvest com correção", "24.90"),
    ("Curso de Interpretação de Texto", "Curso", "Curso em Vídeo", "Estratégias para responder questões de leitura crítica", "44.90"),
    ("Pacote 10 Simulados Vestibular", "Pacote", "Pacote de Simulados", "10 simulados completos com análise por área", "79.90"),
    ("Notion Template - Metas e Hábitos", "Produto Digital", "Template Notion", "Tracker de hábitos, metas trimestrais e revisão semanal", "18.90"),
    ("Mapa Mental - Termodinâmica", "Produto Digital", "Mapa Mental", "Leis, ciclos e diagramas resumidos em mapa visual", "9.90"),
    ("Apostila Inglês Instrumental", "Ebook", "Guia da Matéria", "Estratégias de leitura, prefixos, sufixos e cognatos", "21.90"),
    ("Simulado Concurso Bancário", "Avaliação", "Simulado", "Simulado nível Banco do Brasil/Caixa com gabarito", "26.90"),
    ("Curso de Argumentação para Redação", "Curso", "Curso de Redação", "Como construir teses, argumentos e propostas de intervenção", "54.90"),
    ("Banco de Questões - Atualidades", "Avaliação", "Pacote de Questões", "Atualidades dos últimos 12 meses com contextualização", "16.90"),
    ("Resumo - Sociologia Clássica", "Ebook", "Resumo da Matéria", "Durkheim, Weber e Marx em linguagem direta", "14.90"),
    ("Planner Diário 2026", "Produto Digital", "Planner de Estudos", "Planner diário com pomodoro, metas e revisão", "25.90"),
    ("Guia - Análise Sintática Sem Mistério", "Ebook", "Guia da Matéria", "Período simples e composto com exercícios graduais", "19.90"),
    ("Simulado IME - Física e Cálculo", "Avaliação", "Simulado", "Simulado nível IME com questões discursivas", "22.90"),
    ("Curso Express - Funções Matemáticas", "Curso", "Curso em Vídeo", "Função afim, quadrática, exponencial e logarítmica", "42.90"),
    ("Notion Template - Integração com Anki", "Produto Digital", "Template Notion", "Sistema unificando flashcards, metas e revisões", "23.90"),
    ("Pacote Apostilas - Ciências da Natureza", "Pacote", "Pacote de Estudos", "Biologia, Química e Física em um único pacote", "74.90"),
    ("Mapa Mental - Era Vargas", "Produto Digital", "Mapa Mental", "Linha do tempo e personagens da Era Vargas", "10.90"),
    ("Apostila Geografia Física", "Ebook", "Guia da Matéria", "Climatologia, geomorfologia e biomas brasileiros", "26.90"),
    ("Simulado ENEM - Linguagens e Códigos", "Avaliação", "Simulado", "Simulado focado em LC com gabarito comentado", "13.90"),
    ("Curso de Resolução de Provas", "Curso", "Curso de Estratégia", "Métodos para eliminar alternativas e administrar tempo", "47.90"),
    ("Banco de Questões - Cinemática", "Avaliação", "Pacote de Questões", "Movimento uniforme, MUV e queda livre com solução", "17.90"),
    ("Resumo Estratégico - Filosofia Moderna", "Ebook", "Resumo da Matéria", "Descartes, Hume, Kant e os iluministas resumidos", "15.90"),
    ("Planner Mensal - Foco Total", "Produto Digital", "Planner de Estudos", "Planner minimalista com bloco de foco profundo", "13.90"),
    ("Guia Mente Tranquila - Antiansiedade no Vestibular", "Ebook", "Guia de Bem-Estar", "Técnicas de respiração, mindfulness e gestão de pressão", "18.90"),
]

for _idx, (_name, _type, _subtype, _description, _price) in enumerate(_EXTRA_CATALOG):
    CATALOG.append(
        {
            "name": _name,
            "type": _type,
            "subtype": _subtype,
            "description": _description,
            "price": _price,
            "image_url": _FREE_IMAGES[_idx % len(_FREE_IMAGES)],
        }
    )


_REVIEW_AUTHORS = [
    "Ana Beatriz", "Pedro Henrique", "Larissa Souza", "Gustavo Lima",
    "Mariana Alves", "Rafael Costa", "Camila Ribeiro", "Lucas Martins",
    "Júlia Ferreira", "Bruno Carvalho", "Isabela Rocha", "Felipe Andrade",
    "Beatriz Nunes", "Thiago Moreira", "Sofia Cardoso", "Vinícius Barbosa",
    "Helena Pinto", "Matheus Teixeira", "Letícia Gomes", "Diego Silveira",
]

_REVIEW_TEMPLATES = [
    (5, "Excelente material! Ajudou demais nos meus estudos. Recomendo muito."),
    (5, "Conteúdo super completo e didático. Valeu cada centavo."),
    (5, "Surpreendente! Mudou minha forma de estudar, organização incrível."),
    (5, "Top demais, linguagem clara e exemplos práticos. Nota 10."),
    (4, "Muito bom, atendeu minhas expectativas. Pequenos detalhes a melhorar."),
    (4, "Material consistente e bem estruturado. Recomendo."),
    (4, "Gostei bastante, só senti falta de mais exercícios resolvidos."),
    (4, "Bom custo-benefício, conteúdo claro e direto ao ponto."),
    (3, "Razoável. Cobre o básico, mas esperava mais profundidade."),
    (3, "É útil, mas o layout poderia ser mais intuitivo."),
    (5, "Já indiquei pra minha turma toda. Conteúdo de qualidade."),
    (4, "Está me ajudando bastante na rotina de revisão. Vale a pena."),
]


def _seed_reviews_for(product: Product) -> int:
    if product.reviews.exists():
        return 0
    seed_int = int(hashlib.md5(product.name.encode("utf-8")).hexdigest(), 16)
    count = 3 + (seed_int % 6)  # 3..8 reviews
    created = 0
    for i in range(count):
        idx = (seed_int >> (i * 3)) % len(_REVIEW_TEMPLATES)
        author_idx = (seed_int >> (i * 4 + 1)) % len(_REVIEW_AUTHORS)
        rating, comment = _REVIEW_TEMPLATES[idx]
        Review.objects.create(
            product=product,
            author=_REVIEW_AUTHORS[author_idx],
            rating=rating,
            comment=comment,
        )
        created += 1
    return created


class Command(BaseCommand):
    help = "Idempotente: sincroniza o catálogo de produtos a partir do CATALOG embutido."

    def handle(self, *args, **options) -> None:
        created = updated = 0
        reviews_created = 0
        for entry in CATALOG:
            defaults = {
                "type": entry["type"],
                "subtype": entry["subtype"],
                "description": entry["description"],
                "price": Decimal(entry["price"]),
                "image_url": entry.get("image_url", ""),
            }
            product, was_created = Product.objects.update_or_create(
                name=entry["name"], defaults=defaults
            )
            if was_created:
                created += 1
            else:
                updated += 1
            reviews_created += _seed_reviews_for(product)
        self.stdout.write(
            self.style.SUCCESS(
                f"Catalog sync OK — {created} created, {updated} updated, "
                f"{reviews_created} reviews seeded"
            )
        )
