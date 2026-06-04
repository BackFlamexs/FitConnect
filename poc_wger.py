"""
POC - WGER API: verificar que tipo de midia (GIF, video, imagem) esta disponivel.

Testa 5 exercicios: Supino Reto, Flexao de Braco, Agachamento, Rosca Direta, Triceps Corda.
NAO altera Supabase. NAO altera o app.

Uso:
  pip install requests
  python poc_wger.py
"""

import requests

BASE = "https://wger.de/api/v2"

EXERCICIOS = {
    "Supino Reto":      "bench press",
    "Flexao de Braco":  "push-up",
    "Agachamento":      "squat",
    "Rosca Direta":     "barbell curl",
    "Triceps Corda":    "cable pushdown",
}

def buscar_exercicio_base_id(termo: str) -> list[dict]:
    """Busca exercicios pelo nome em ingles, retorna lista de {id, name}."""
    r = requests.get(
        f"{BASE}/exercise/search/",
        params={"term": termo, "language": "english", "format": "json"},
        timeout=15,
    )
    if r.status_code != 200:
        print(f"  Erro na busca '{termo}': {r.status_code} {r.text[:100]}")
        return []
    data = r.json()
    sugestoes = data.get("suggestions", [])
    resultados = []
    for s in sugestoes[:3]:
        resultados.append({
            "base_id": s.get("data", {}).get("base_id"),
            "name":    s.get("value", "?"),
        })
    return resultados


def buscar_exerciseinfo(base_id: int) -> dict:
    """Busca info completa do exercicio incluindo imagens e videos."""
    r = requests.get(f"{BASE}/exerciseinfo/{base_id}/?format=json", timeout=15)
    if r.status_code != 200:
        return {}
    return r.json()


def inspecionar_midia(info: dict) -> dict:
    """Extrai todas as URLs de midia do exerciceinfo."""
    resultado = {
        "imagens": [],
        "videos":  [],
    }

    for img in info.get("images", []):
        resultado["imagens"].append({
            "url":       img.get("image", ""),
            "is_main":   img.get("is_main", False),
            "style":     img.get("style", ""),
        })

    for vid in info.get("videos", []):
        resultado["videos"].append({
            "video":  vid.get("video", ""),
            "size":   vid.get("size", ""),
            "codec":  vid.get("codec", ""),
            "width":  vid.get("width", ""),
            "height": vid.get("height", ""),
        })

    return resultado


def checar_url(url: str) -> str:
    """Faz HEAD request e retorna Content-Type ou erro."""
    if not url:
        return "URL vazia"
    try:
        r = requests.head(url, timeout=8, allow_redirects=True)
        ct = r.headers.get("Content-Type", "?")
        return f"{r.status_code} | {ct}"
    except Exception as e:
        return f"ERR: {str(e)[:50]}"


# ─────────────────────────────────────────────────────────────────────────────

print("=" * 70)
print("  POC - WGER API: verificacao de midia")
print("=" * 70)

for nome_pt, termo_en in EXERCICIOS.items():
    print(f"\n{'='*70}")
    print(f"  {nome_pt}  (busca: '{termo_en}')")
    print(f"{'='*70}")

    candidatos = buscar_exercicio_base_id(termo_en)
    if not candidatos:
        print("  Nenhum resultado encontrado.")
        continue

    print(f"  Resultados encontrados: {len(candidatos)}")
    for c in candidatos:
        print(f"    base_id={c['base_id']}  nome='{c['name']}'")

    # Pega o primeiro candidato
    base_id = candidatos[0]["base_id"]
    if not base_id:
        print("  base_id nulo, pulando.")
        continue

    info = buscar_exerciseinfo(base_id)
    if not info:
        print(f"  Nao foi possivel obter exerciseinfo para base_id={base_id}")
        continue

    midia = inspecionar_midia(info)

    print(f"\n  IMAGENS ({len(midia['imagens'])} encontradas):")
    if midia["imagens"]:
        for img in midia["imagens"]:
            status = checar_url(img["url"])
            main_tag = " [MAIN]" if img["is_main"] else ""
            print(f"    [{status}]{main_tag}")
            print(f"    URL: {img['url']}")
    else:
        print("    Nenhuma imagem disponivel.")

    print(f"\n  VIDEOS ({len(midia['videos'])} encontrados):")
    if midia["videos"]:
        for vid in midia["videos"]:
            url = vid["video"]
            status = checar_url(url)
            print(f"    [{status}]")
            print(f"    URL   : {url}")
            print(f"    Codec : {vid['codec']}  |  {vid['width']}x{vid['height']}  |  {vid['size']} bytes")
    else:
        print("    Nenhum video disponivel.")

print(f"\n{'='*70}")
print("  RESUMO: tipos de midia encontrados acima.")
print("  Se Content-Type = 'image/gif'      -> GIF animado")
print("  Se Content-Type = 'video/mp4'      -> video curto (melhor que JPG)")
print("  Se Content-Type = 'image/jpeg'     -> foto estatica (igual ao atual)")
print(f"{'='*70}\n")
