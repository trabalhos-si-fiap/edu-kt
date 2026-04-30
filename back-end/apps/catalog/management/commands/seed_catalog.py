from decimal import Decimal

from django.core.management.base import BaseCommand

from apps.catalog.models import Product

CATALOG: list[dict] = [
    {
        "name": "Planner ENEM 2026",
        "type": "Digital Product",
        "subtype": "Study Planner",
        "description": "Planner completo com organização semanal, metas e revisão espaçada para ENEM",
        "price": "29.90",
    },
    {
        "name": "Notion Template - Rotina de Estudos",
        "type": "Digital Product",
        "subtype": "Notion Template",
        "description": "Template pronto no Notion com controle de matérias, tarefas e progresso",
        "price": "19.90",
    },
    {
        "name": "Guia Completo ENEM",
        "type": "Ebook",
        "subtype": "Exam Prep Guide",
        "description": "Guia estratégico com técnicas de prova, gestão de tempo e redação",
        "price": "39.90",
    },
    {
        "name": "Simulado preparatorio para ENEM",
        "type": "Assessment",
        "subtype": "Mock Exam",
        "description": "Simulado completo com correção automática e análise de desempenho",
        "price": "14.90",
    },
    {
        "name": "BioVest - Questões Biologia",
        "type": "Assessment",
        "subtype": "Question Pack",
        "description": "Banco com +300 questões de biologia focadas em vestibular",
        "price": "24.90",
    },
    {
        "name": "Redação Nota 1000",
        "type": "Course",
        "subtype": "Writing",
        "description": "Curso com estrutura, repertório e exemplos de redações nota máxima",
        "price": "49.90",
    },
    {
        "name": "Planner de Revisão Inteligente",
        "type": "Digital Product",
        "subtype": "Study Planner",
        "description": "Sistema baseado em repetição espaçada para maximizar memorização",
        "price": "27.90",
    },
    {
        "name": "Notion Template - Controle de Simulados",
        "type": "Digital Product",
        "subtype": "Notion Template",
        "description": "Template para acompanhar desempenho em simulados e evolução",
        "price": "17.90",
    },
    {
        "name": "Física Essencial para Vestibular",
        "type": "Ebook",
        "subtype": "Subject Guide",
        "description": "Conteúdo direto ao ponto com fórmulas e exercícios resolvidos",
        "price": "34.90",
    },
    {
        "name": "Simulado preparatorio para Fuvest",
        "type": "Assessment",
        "subtype": "Mock Exam",
        "description": "Simulado específico para Fuvest com nível avançado",
        "price": "16.90",
    },
    {
        "name": "Matemática Básica para ENEM",
        "type": "Course",
        "subtype": "Video Course",
        "description": "Curso em vídeo com fundamentos e exercícios práticos",
        "price": "59.90",
    },
    {
        "name": "Cronograma 90 dias ENEM",
        "type": "Digital Product",
        "subtype": "Study Planner",
        "description": "Plano intensivo de 90 dias com foco em revisão e prática",
        "price": "21.90",
    },
    {
        "name": "BioVest - Revisão Rápida",
        "type": "Ebook",
        "subtype": "Subject Summary",
        "description": "Resumo estratégico de biologia para revisão pré-prova",
        "price": "18.90",
    },
    {
        "name": "Pacote Simulados ENEM (5 provas)",
        "type": "Bundle",
        "subtype": "Mock Exams Pack",
        "description": "5 simulados completos com análise de desempenho",
        "price": "49.90",
    },
    {
        "name": "Template Notion - Vida do Vestibulando",
        "type": "Digital Product",
        "subtype": "Notion Template",
        "description": "Organização completa: estudos, hábitos, metas e saúde mental",
        "price": "24.90",
    },
]


class Command(BaseCommand):
    help = "Idempotente: sincroniza o catálogo de produtos a partir do CATALOG embutido."

    def handle(self, *args, **options) -> None:
        created = updated = 0
        for entry in CATALOG:
            defaults = {
                "type": entry["type"],
                "subtype": entry["subtype"],
                "description": entry["description"],
                "price": Decimal(entry["price"]),
            }
            _, was_created = Product.objects.update_or_create(
                name=entry["name"], defaults=defaults
            )
            if was_created:
                created += 1
            else:
                updated += 1
        self.stdout.write(
            self.style.SUCCESS(f"Catalog sync OK — {created} created, {updated} updated")
        )
