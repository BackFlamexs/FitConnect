"""
Script LEGADO: popula gif_url com imagens estaticas JPG do free-exercise-db.
Este script foi substituido por update_gifs_exercisedb.py, que usa GIFs animados.
Mantido apenas para referencia historica.

Variaveis de ambiente necessarias:
  SUPABASE_URL              - URL do projeto Supabase
  SUPABASE_SERVICE_ROLE_KEY - Chave de servico do Supabase

Como usar:
  pip install requests python-dotenv
  python populate_gifs.py
"""

import os
import requests

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

SUPABASE_URL         = os.environ.get("SUPABASE_URL", "")
SUPABASE_SERVICE_KEY = os.environ.get("SUPABASE_SERVICE_ROLE_KEY", "")

if not SUPABASE_URL or not SUPABASE_SERVICE_KEY:
    print("ERRO: defina SUPABASE_URL e SUPABASE_SERVICE_ROLE_KEY no arquivo .env")
    raise SystemExit(1)

IMG_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

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
    print("\n=== FitConnect - Populando imagens de exercicios (LEGADO - JPG) ===\n")
    print("ATENCAO: use update_gifs_exercisedb.py para GIFs animados.\n")

    banco = carregar_banco_exercicios()
    if not banco:
        return

    exercicios = buscar_exercicios_supabase()
    if not exercicios:
        print("Nenhum exercicio encontrado no Supabase.")
        return

    contador_categoria: dict[str, int] = {}
    ok = 0
    falhou = 0

    banco_por_musculo: dict[str, list] = {}
    for ex in banco:
        for m in ex.get("primaryMuscles", []):
            banco_por_musculo.setdefault(m.lower(), []).append(ex)

    for ex in exercicios:
        nome = ex.get("nome", "")
        categoria = ex.get("categoria", "")
        ex_id = ex.get("id")

        img_url = ""
        termo = NOMES_PT_PARA_BUSCA.get(nome, "").lower()
        if termo:
            for item in banco:
                if termo in item.get("name", "").lower() and item.get("images"):
                    img_url = IMG_BASE + item["images"][0]
                    break

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
