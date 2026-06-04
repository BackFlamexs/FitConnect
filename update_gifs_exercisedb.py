"""
Atualiza a coluna gif_url da tabela galeria_exercicios com GIFs animados.

Fonte dos GIFs:
  Dataset publico do bootstrapping-lab/exercisedb-api (GitHub), 1500 exercicios.
  GIFs hospedados em: https://static.exercisedb.dev/media/{exerciseId}.gif
  Nenhuma autenticacao necessaria.

Por padrao executa em modo DRY-RUN (nao salva nada).
Use --apply para executar o update real no Supabase.

Variaveis de ambiente necessarias (arquivo .env):
  SUPABASE_URL              - URL do projeto Supabase
  SUPABASE_SERVICE_ROLE_KEY - Chave de servico do Supabase

Instalacao:
  pip install requests python-dotenv

Uso:
  python update_gifs_exercisedb.py           # dry-run
  python update_gifs_exercisedb.py --apply   # update real
"""

import os
import sys
import argparse
import requests

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

# -- Credenciais ---------------------------------------------------------------

SUPABASE_URL              = os.environ.get("SUPABASE_URL", "")
SUPABASE_SERVICE_ROLE_KEY = os.environ.get("SUPABASE_SERVICE_ROLE_KEY", "")

HEADERS_SUPABASE = {
    "apikey":        SUPABASE_SERVICE_ROLE_KEY,
    "Authorization": f"Bearer {SUPABASE_SERVICE_ROLE_KEY}",
    "Content-Type":  "application/json",
    "Prefer":        "return=minimal",
}

# Dataset publico com 1500 exercicios e gifUrl reais (static.exercisedb.dev)
DATASET_URL = (
    "https://raw.githubusercontent.com/bootstrapping-lab/"
    "exercisedb-api/main/src/data/exercises.json"
)

# -- Mapeamento: nome PT -> termos de busca em ingles --------------------------

NOMES_PT_PARA_BUSCA: dict[str, list[str]] = {
    # Peito
    "Supino Reto":              ["barbell bench press", "bench press"],
    "Supino Inclinado":         ["incline barbell bench press", "incline bench press", "incline dumbbell press"],
    "Supino Declinado":         ["decline barbell bench press", "decline bench press"],
    "Supino Fechado":           ["close-grip barbell bench press", "close grip bench press"],
    "Crucifixo":                ["dumbbell fly", "dumbbell flyes", "cable fly"],
    "Crucifixo Inclinado":      ["incline dumbbell fly", "incline dumbbell flyes"],
    "Flexao de Braco":          ["push-up", "pushup"],
    "Flexão de Braço":          ["push-up", "pushup"],
    "Cross Over":               ["cable crossover", "cable cross-over", "cable cross over"],
    # Costas
    "Pulldown":                 ["lat pulldown", "pulldown"],
    "Puxada Frente":            ["lat pulldown", "cable pulldown"],
    "Puxada Frontal":           ["lat pulldown", "cable pulldown"],
    "Puxada Atras":             ["behind the neck pulldown", "lat pulldown"],
    "Remada Curvada":           ["barbell bent over row", "bent over barbell row"],
    "Remada Unilateral":        ["dumbbell bent over row", "one arm dumbbell row"],
    "Remada Baixa":             ["seated cable row", "cable seated row"],
    "Barra Fixa":               ["pull-up", "pullup", "chin-up"],
    "Levantamento Terra":       ["barbell deadlift", "deadlift"],
    "Stiff":                    ["romanian deadlift", "stiff-legged deadlift", "stiff leg deadlift"],
    "Good Morning":             ["good morning", "barbell good morning"],
    # Pernas
    "Agachamento":              ["barbell squat", "squat"],
    "Agachamento Livre":        ["barbell squat", "squat"],
    "Agachamento Bulgaro":      ["bulgarian split squat", "split squat"],
    "Agachamento Búlgaro":      ["bulgarian split squat", "split squat"],
    "Agachamento Sumô":         ["sumo squat", "sumo deadlift"],
    "Agachamento Sumo":         ["sumo squat", "sumo deadlift"],
    "Leg Press":                ["leg press", "sled leg press"],
    "Cadeira Extensora":        ["leg extension", "seated leg extension"],
    "Mesa Flexora":             ["leg curl", "lying leg curl", "seated leg curl"],
    "Afundo":                   ["dumbbell lunge", "barbell lunge", "lunge"],
    "Afundo com Haltere":       ["dumbbell lunge", "lunge"],
    "Panturrilha em Pe":        ["standing calf raise", "calf raise"],
    "Panturrilha em Pé":        ["standing calf raise", "calf raise"],
    "Panturrilha Sentado":      ["seated calf raise"],
    "Hack Squat":               ["hack squat", "barbell hack squat"],
    # Biceps
    "Rosca Direta":             ["barbell curl", "ez bar curl"],
    "Rosca Direta com Haltere": ["dumbbell curl", "dumbbell bicep curl", "alternating dumbbell curl"],
    "Rosca Martelo":            ["hammer curl", "dumbbell hammer curl"],
    "Rosca Concentrada":        ["concentration curl", "dumbbell concentration curl"],
    "Rosca Scott":              ["preacher curl", "ez bar preacher curl", "barbell preacher curl"],
    "Rosca Inclinada":          ["incline dumbbell curl", "incline curl"],
    # Triceps
    "Triceps Testa":            ["barbell skull crusher", "skull crusher", "lying tricep extension"],
    "Tríceps Testa":            ["barbell skull crusher", "skull crusher", "lying tricep extension"],
    "Triceps Corda":            ["cable pushdown", "triceps pushdown", "rope pushdown"],
    "Tríceps Corda":            ["cable pushdown", "triceps pushdown", "rope pushdown"],
    "Triceps Mergulho":         ["tricep dip", "parallel bar dip", "dip"],
    "Tríceps Mergulho":         ["tricep dip", "parallel bar dip", "dip"],
    "Triceps Frances":          ["tricep extension", "overhead tricep extension", "french press"],
    "Tríceps Francês":          ["tricep extension", "overhead tricep extension", "french press"],
    "Triceps Coice":            ["dumbbell kickback", "tricep kickback"],
    "Tríceps Coice":            ["dumbbell kickback", "tricep kickback"],
    # Ombros
    "Desenvolvimento":          ["overhead press", "barbell overhead press", "military press"],
    "Desenvolvimento Ombros":   ["overhead press", "barbell overhead press", "military press"],
    "Desenvolvimento Halter":   ["dumbbell shoulder press", "dumbbell overhead press"],
    "Elevacao Lateral":         ["lateral raise", "dumbbell lateral raise"],
    "Elevação Lateral":         ["lateral raise", "dumbbell lateral raise"],
    "Elevacao Frontal":         ["front raise", "dumbbell front raise", "barbell front raise"],
    "Elevação Frontal":         ["front raise", "dumbbell front raise", "barbell front raise"],
    "Encolhimento":             ["barbell shrug", "dumbbell shrug", "shrug"],
    "Face Pull":                ["face pull", "cable face pull"],
    "Arnold Press":             ["arnold press", "dumbbell arnold press"],
    # Cardio / Funcional
    "Corrida":                  ["run", "jogging", "treadmill"],
    "Corrida na Esteira":       ["treadmill run", "treadmill"],
    "Corda Naval":              ["battle rope", "battle rope slam"],
    "Burpee":                   ["burpee"],
    "Polichinelo":              ["jumping jack"],
    "Bicicleta Ergometrica":    ["stationary bike", "cycling", "bike"],
    "Bicicleta Ergométrica":    ["stationary bike", "cycling", "bike"],
    "Pular Corda":              ["jump rope", "rope jumping"],
    "Remada Ergometrica":       ["rowing machine", "row"],
    # Abdomen
    "Abdominal":                ["crunch", "sit-up"],
    "Prancha":                  ["plank", "forearm plank"],
    "Abdominal Bicicleta":      ["bicycle crunch"],
    "Elevacao de Pernas":       ["leg raise", "hanging leg raise", "lying leg raise"],
    "Elevação de Pernas":       ["leg raise", "hanging leg raise", "lying leg raise"],
    "Russian Twist":            ["russian twist"],
    "Abdominal Infra":          ["reverse crunch"],
}

# Mapeamento: categoria PT -> bodyParts ExerciseDB (fallback)
CATEGORIAS_PARA_BODYPART: dict[str, list[str]] = {
    "Peito":     ["chest"],
    "Costas":    ["back"],
    "Pernas":    ["upper legs", "lower legs"],
    "Biceps":    ["upper arms"],
    "Bíceps":    ["upper arms"],
    "Triceps":   ["upper arms"],
    "Tríceps":   ["upper arms"],
    "Ombros":    ["shoulders"],
    "Cardio":    ["cardio"],
    "Abdomen":   ["waist"],
    "Abdômen":   ["waist"],
    "Funcional": ["cardio"],
    "HIIT":      ["cardio"],
    "Bracos":    ["upper arms", "lower arms"],
    "Braços":    ["upper arms", "lower arms"],
    "Gluteos":   ["upper legs"],
    "Glúteos":   ["upper legs"],
}


# -- Dataset -------------------------------------------------------------------

def carregar_dataset() -> list[dict]:
    """Baixa o dataset publico do GitHub (1500 exercicios com gifUrl)."""
    print(f"Baixando dataset de exercicios...")
    print(f"  {DATASET_URL}")
    try:
        r = requests.get(DATASET_URL, timeout=30)
        r.raise_for_status()
        data = r.json()
    except Exception as e:
        print(f"  ERRO ao baixar dataset: {e}")
        return []

    if not isinstance(data, list):
        print(f"  Formato inesperado: {type(data).__name__}")
        return []

    com_gif = sum(1 for ex in data if ex.get("gifUrl"))
    print(f"  {len(data)} exercicios carregados, {com_gif} com gifUrl.\n")
    return data


def indexar_dataset(data: list[dict]) -> tuple[dict, dict]:
    """Cria indices por nome (lowercase) e por bodyPart."""
    por_nome:     dict[str, dict] = {}
    por_bodypart: dict[str, list[dict]] = {}

    for ex in data:
        nome = ex.get("name", "").lower().strip()
        if nome:
            por_nome[nome] = ex

        for bp in ex.get("bodyParts", []):
            bp = bp.lower().strip()
            if bp:
                por_bodypart.setdefault(bp, []).append(ex)

    return por_nome, por_bodypart


def buscar_gif(
    nome_pt: str,
    categoria: str,
    por_nome: dict,
    por_bodypart: dict,
    contador_bp: dict[str, int],
) -> str:
    """Retorna gifUrl para o exercicio. Tenta match exato, parcial e fallback."""

    termos = NOMES_PT_PARA_BUSCA.get(nome_pt, [])
    if not termos:
        # Se nome tem acento e nao esta no dict, tenta versao sem acento
        nome_sem_acento = (
            nome_pt
            .replace("ã", "a").replace("ç", "c").replace("é", "e")
            .replace("ê", "e").replace("â", "a").replace("á", "a")
            .replace("ó", "o").replace("ô", "o").replace("í", "i")
            .replace("ú", "u").replace("Ã", "A").replace("Ç", "C")
        )
        termos = NOMES_PT_PARA_BUSCA.get(nome_sem_acento, [])

    for termo in termos:
        termo_lower = termo.lower()

        # Match exato
        if termo_lower in por_nome:
            ex = por_nome[termo_lower]
            gif = ex.get("gifUrl", "")
            if gif:
                return gif

        # Match parcial — o termo deve estar contido no nome do exercicio
        for nome_en, ex in por_nome.items():
            if termo_lower in nome_en:
                gif = ex.get("gifUrl", "")
                if gif:
                    return gif

    # Fallback: exercicio aleatorio do bodyPart correto da categoria
    bodyparts = CATEGORIAS_PARA_BODYPART.get(categoria, [])
    for bp in bodyparts:
        lista = por_bodypart.get(bp, [])
        if lista:
            idx = contador_bp.get(bp, 0)
            ex  = lista[idx % len(lista)]
            gif = ex.get("gifUrl", "")
            if gif:
                contador_bp[bp] = idx + 1
                return gif

    return ""


# -- Supabase ------------------------------------------------------------------

def buscar_exercicios_supabase() -> list[dict]:
    url = f"{SUPABASE_URL}/rest/v1/galeria_exercicios"
    try:
        r = requests.get(
            url,
            headers=HEADERS_SUPABASE,
            params={"select": "id,nome,categoria,gif_url", "order": "id.asc"},
            timeout=15,
        )
    except requests.RequestException as e:
        print(f"Erro de conexao com Supabase: {e}")
        return []

    if r.status_code == 200:
        return r.json()
    print(f"Erro Supabase {r.status_code}: {r.text[:200]}")
    return []


def atualizar_gif_url(ex_id: int, gif_url: str) -> bool:
    url = f"{SUPABASE_URL}/rest/v1/galeria_exercicios"
    try:
        r = requests.patch(
            url,
            headers=HEADERS_SUPABASE,
            params={"id": f"eq.{ex_id}"},
            json={"gif_url": gif_url},
            timeout=10,
        )
    except requests.RequestException as e:
        print(f"  Erro patch id={ex_id}: {e}")
        return False
    return r.status_code == 204


# -- Main ----------------------------------------------------------------------

def validar_env() -> bool:
    ok = True
    if not SUPABASE_URL:
        print("ERRO: variavel SUPABASE_URL nao definida.")
        ok = False
    if not SUPABASE_SERVICE_ROLE_KEY:
        print("ERRO: variavel SUPABASE_SERVICE_ROLE_KEY nao definida.")
        ok = False
    if not ok:
        print("\nCrie um arquivo .env baseado em .env.example e preencha os valores.")
    return ok


def main():
    parser = argparse.ArgumentParser(
        description="Atualiza gif_url da galeria_exercicios com GIFs animados (static.exercisedb.dev)."
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Executa o update real no Supabase (sem esta flag roda em dry-run).",
    )
    args = parser.parse_args()

    modo = "APPLY" if args.apply else "DRY-RUN"
    print(f"\n{'='*60}")
    print(f"  FitConnect - Atualizacao de GIFs (static.exercisedb.dev)")
    print(f"  Modo: {modo}")
    print(f"{'='*60}\n")

    if not validar_env():
        sys.exit(1)

    # 1. Baixar dataset
    dataset = carregar_dataset()
    if not dataset:
        print("Nao foi possivel carregar o dataset.")
        sys.exit(1)

    # 2. Indexar
    por_nome, por_bodypart = indexar_dataset(dataset)
    print(f"  {len(por_nome)} exercicios indexados por nome.")
    print(f"  bodyParts disponiveis: {sorted(por_bodypart.keys())}\n")

    # 3. Buscar exercicios do Supabase
    print("Buscando exercicios do Supabase...")
    exercicios_supabase = buscar_exercicios_supabase()
    if not exercicios_supabase:
        print("Nenhum exercicio encontrado no Supabase.")
        sys.exit(1)
    print(f"  {len(exercicios_supabase)} exercicios encontrados.\n")

    # 4. Processar cada exercicio
    contador_bp: dict[str, int] = {}
    resultados:  list[dict] = []

    print(f"{'-'*60}")
    print(f"{'#':>4}  {'Nome':<30} {'GIF':>5}  {'Tipo'}")
    print(f"{'-'*60}")

    for ex in exercicios_supabase:
        nome      = ex.get("nome", "")
        categoria = ex.get("categoria", "")
        ex_id     = ex.get("id")
        gif_atual = ex.get("gif_url", "")

        gif_novo = buscar_gif(nome, categoria, por_nome, por_bodypart, contador_bp)

        # Determinar se veio de match exato, parcial ou fallback
        tipo = "---"
        if gif_novo:
            termos = NOMES_PT_PARA_BUSCA.get(nome, [])
            for t in termos:
                if t.lower() in por_nome:
                    tipo = "exato"
                    break
            if tipo == "---":
                tipo = "parcial" if termos else "fallback"

        encontrou = bool(gif_novo)
        mudou     = encontrou and gif_novo != gif_atual

        resultados.append({
            "id":        ex_id,
            "nome":      nome,
            "categoria": categoria,
            "gif_atual": gif_atual,
            "gif_novo":  gif_novo,
            "encontrou": encontrou,
            "mudou":     mudou,
            "tipo":      tipo,
        })

        status = "[GIF]" if encontrou else "[---]"
        print(f"{ex_id:>4}  {nome:<30} {status}  {tipo}")

    print(f"{'-'*60}\n")

    # 5. Resumo
    total          = len(resultados)
    com_gif        = sum(1 for r in resultados if r["encontrou"])
    sem_gif        = total - com_gif
    para_atualizar = sum(1 for r in resultados if r["mudou"])
    por_tipo       = {}
    for r in resultados:
        if r["encontrou"]:
            por_tipo[r["tipo"]] = por_tipo.get(r["tipo"], 0) + 1

    print("RESUMO:")
    print(f"  Total de exercicios analisados : {total}")
    print(f"  Exercicios com GIF encontrado  : {com_gif}")
    print(f"  Exercicios sem correspondencia : {sem_gif}")
    print(f"  Registros que seriam alterados : {para_atualizar}")
    if por_tipo:
        print(f"  Por tipo de match:")
        for tipo, cnt in sorted(por_tipo.items()):
            print(f"    {tipo}: {cnt}")
    print()

    # 6. Exemplos de mudancas
    exemplos = [r for r in resultados if r["mudou"]][:5]
    if exemplos:
        print("Exemplos de mudancas:")
        for r in exemplos:
            print(f"  [{r['id']}] {r['nome']}  ({r['tipo']})")
            print(f"       ANTES: {r['gif_atual'] or '(vazio)'}")
            print(f"       APOS : {r['gif_novo']}")
            print()

    # 7. Sem correspondencia
    sem = [r for r in resultados if not r["encontrou"]]
    if sem:
        print(f"Exercicios sem correspondencia ({len(sem)}):")
        for r in sem:
            print(f"  - {r['nome']} ({r['categoria']})")
        print()

    # 8. Dry-run ou apply
    if not args.apply:
        print("Modo DRY-RUN: nenhum dado foi alterado.")
        print("Para aplicar, execute: python update_gifs_exercisedb.py --apply\n")
        return

    print(f"Prestes a atualizar {para_atualizar} registros no Supabase.")
    resposta = input("Confirmar? (s/N): ").strip().lower()
    if resposta != "s":
        print("Operacao cancelada.")
        return

    print("\nAplicando updates...\n")
    ok     = 0
    falhou = 0

    for r in resultados:
        if not r["mudou"]:
            continue
        if atualizar_gif_url(r["id"], r["gif_novo"]):
            print(f"  [OK]  {r['nome']}")
            ok += 1
        else:
            print(f"  [ERR] {r['nome']} (id={r['id']})")
            falhou += 1

    print(f"\n{'='*60}")
    print(f"  RELATORIO FINAL")
    print(f"{'='*60}")
    print(f"  Fonte dos GIFs       : static.exercisedb.dev")
    print(f"  Dataset              : bootstrapping-lab/exercisedb-api")
    print(f"  Exercicios analisados: {total}")
    print(f"  Atualizados com exito: {ok}")
    print(f"  Falhas               : {falhou}")
    print(f"  Sem correspondencia  : {sem_gif}")
    print(f"{'='*60}\n")

    if ok > 0:
        print("Exemplos de URLs gravadas:")
        for r in [r for r in resultados if r["mudou"]][:3]:
            print(f"  {r['nome']}: {r['gif_novo']}")
        print()


if __name__ == "__main__":
    main()