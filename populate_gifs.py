"""
Script para popular a coluna gif_url da tabela galeria_exercicios no Supabase.
Fonte das imagens: free-exercise-db (GitHub) - 873 exercicios gratuitos

Como usar:
  python populate_gifs.py

Requisito: pip install requests
"""

import requests

SUPABASE_URL         = "https://gltouzhsqtvoinphhbmv.supabase.co"
SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdsdG91emhzcXR2b2lucGhoYm12Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3Mzg3MjIyMywiZXhwIjoyMDg5NDQ4MjIzfQ.eh50_w_e3S4fgh1Qto6tkN4IXrLX63Mp_AANxHhDVbY"

IMG_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

# Mapeamento: nome PT → termo de busca em ingles (parcial, case-insensitive)
NOMES_PT_PARA_BUSCA = {
    "Supino Reto":           "barbell bench press",
    "Supino Inclinado":      "incline dumbbell",
    "Supino Fechado":        "close-grip",
    "Crucifixo":             "dumbbell fly",
    "Flexao de Braco":       "push-up",
    "Flexão de Braço":       "push-up",
    "Pulldown":              "pulldown",
    "Puxada Frente":         "lat pulldown",
    "Puxada Frontal":        "lat pulldown",
    "Remada Curvada":        "bent over row",
    "Barra Fixa":            "pull-up",
    "Remada Unilateral":     "one arm row",
    "Agachamento":           "barbell squat",
    "Agachamento Livre":     "barbell squat",
    "Agachamento Bulgaro":   "bulgarian",
    "Agachamento Búlgaro":   "bulgarian",
    "Leg Press":             "leg press",
    "Stiff":                 "romanian deadlift",
    "Cadeira Extensora":     "leg extension",
    "Mesa Flexora":          "leg curl",
    "Afundo":                "lunge",
    "Panturrilha em Pe":     "calf raise",
    "Panturrilha em Pé":     "calf raise",
    "Levantamento Terra":    "deadlift",
    "Rosca Direta":          "barbell curl",
    "Rosca Martelo":         "hammer curl",
    "Rosca Concentrada":     "concentration curl",
    "Rosca Scott":           "preacher curl",
    "Corrida":               "run",
    "Corrida na Esteira":    "run",
    "Corda Naval":           "battle rope",
    "Burpee":                "burpee",
    "Polichinelo":           "jumping jack",
    "Bicicleta Ergometrica": "cycling",
    "Bicicleta Ergométrica": "cycling",
    "Desenvolvimento":       "shoulder press",
    "Desenvolvimento Ombros":"shoulder press",
    "Elevacao Lateral":      "lateral raise",
    "Elevação Lateral":      "lateral raise",
    "Encolhimento":          "shrug",
    "Face Pull":             "face pull",
    "Triceps Testa":         "skull crusher",
    "Tríceps Testa":         "skull crusher",
    "Triceps Corda":         "pushdown",
    "Tríceps Corda":         "pushdown",
    "Triceps Mergulho":      "tricep dip",
    "Tríceps Mergulho":      "tricep dip",
    "Triceps Frances":       "french press",
    "Tríceps Francês":       "french press",
}

# Mapeamento de categorias PT → primaryMuscles no free-exercise-db
CATEGORIAS_PARA_MUSCULOS = {
    "Peito":    ["chest"],
    "Costas":   ["lats", "middle back", "lower back"],
    "Pernas":   ["quadriceps", "hamstrings", "glutes", "calves"],
    "Biceps":   ["biceps"],
    "Bíceps":   ["biceps"],
    "Triceps":  ["triceps"],
    "Tríceps":  ["triceps"],
    "Ombros":   ["shoulders"],
    "Cardio":   [],
    "Bracos":   ["biceps", "forearms"],
    "Braços":   ["biceps", "forearms"],
    "Abdomen":  ["abdominals"],
    "Abdômen":  ["abdominals"],
    "Gluteos":  ["glutes"],
    "Glúteos":  ["glutes"],
}

HEADERS_SUPABASE = {
    "apikey":        SUPABASE_SERVICE_KEY,
    "Authorization": f"Bearer {SUPABASE_SERVICE_KEY}",
    "Content-Type":  "application/json",
    "Prefer":        "return=minimal"
}


def carregar_banco_exercicios():
    print("Baixando banco de exercicios do GitHub...")
    r = requests.get(
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json",
        timeout=30
    )
    if r.status_code == 200:
        data = r.json()
        print(f"  {len(data)} exercicios carregados.")
        return data
    print(f"  Erro: {r.status_code}")
    return []


def buscar_imagem(nome_pt: str, categoria: str, banco: list) -> str:
    """Retorna a URL da imagem mais adequada para o exercicio."""

    # 1a tentativa: busca por nome (parcial, case-insensitive)
    termo = NOMES_PT_PARA_BUSCA.get(nome_pt, "").lower()
    if termo:
        for ex in banco:
            if termo in ex.get("name", "").lower() and ex.get("images"):
                return IMG_BASE + ex["images"][0]

    # 2a tentativa: busca pelo musculo primario da categoria
    musculos = CATEGORIAS_PARA_MUSCULOS.get(categoria, [])
    for ex in banco:
        primary = [m.lower() for m in ex.get("primaryMuscles", [])]
        if any(m in primary for m in musculos) and ex.get("images"):
            return IMG_BASE + ex["images"][0]

    return ""


def buscar_exercicios_supabase():
    url = f"{SUPABASE_URL}/rest/v1/galeria_exercicios"
    r = requests.get(url, headers=HEADERS_SUPABASE,
                     params={"select": "id,nome,categoria", "order": "id.asc"}, timeout=10)
    if r.status_code == 200:
        return r.json()
    print(f"  Erro Supabase: {r.status_code} - {r.text}")
    return []


def atualizar_gif_url(ex_id: int, img_url: str) -> bool:
    url = f"{SUPABASE_URL}/rest/v1/galeria_exercicios"
    r = requests.patch(url, headers=HEADERS_SUPABASE,
                       params={"id": f"eq.{ex_id}"}, json={"gif_url": img_url}, timeout=10)
    return r.status_code == 204


def main():
    print("\n=== FitConnect - Populando imagens de exercicios ===\n")

    banco = carregar_banco_exercicios()
    if not banco:
        return

    print()
    exercicios = buscar_exercicios_supabase()
    if not exercicios:
        print("Nenhum exercicio encontrado no Supabase.")
        return

    # Evita duplicatas: ja processados por categoria (rotacao)
    contador_categoria: dict[str, int] = {}
    ok = 0
    falhou = 0

    # Para o fallback por categoria funcionar corretamente (nao repetir o mesmo exercicio),
    # pre-indexamos o banco por musculo
    banco_por_musculo: dict[str, list] = {}
    for ex in banco:
        for m in ex.get("primaryMuscles", []):
            banco_por_musculo.setdefault(m.lower(), []).append(ex)

    for ex in exercicios:
        nome = ex.get("nome", "")
        categoria = ex.get("categoria", "")
        ex_id = ex.get("id")

        img_url = ""

        # 1a tentativa: busca por nome
        termo = NOMES_PT_PARA_BUSCA.get(nome, "").lower()
        if termo:
            for item in banco:
                if termo in item.get("name", "").lower() and item.get("images"):
                    img_url = IMG_BASE + item["images"][0]
                    break

        # 2a tentativa: fallback por musculo da categoria (rotacao para nao repetir)
        if not img_url:
            musculos = CATEGORIAS_PARA_MUSCULOS.get(categoria, [])
            for musculo in musculos:
                lista = banco_por_musculo.get(musculo, [])
                if lista:
                    idx = contador_categoria.get(musculo, 0)
                    item = lista[idx % len(lista)]
                    if item.get("images"):
                        img_url = IMG_BASE + item["images"][0]
                        contador_categoria[musculo] = idx + 1
                    break

        if img_url and atualizar_gif_url(ex_id, img_url):
            print(f"  [OK]  {nome} ({categoria})")
            ok += 1
        else:
            print(f"  [--]  {nome} ({categoria}) - sem imagem")
            falhou += 1

    print(f"\n=== Concluido: {ok} atualizados, {falhou} sem imagem ===\n")


if __name__ == "__main__":
    main()