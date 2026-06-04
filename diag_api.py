import requests
import os
import json

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

KEY  = os.environ.get("RAPIDAPI_KEY", "")
BASE = "https://exercisedb.p.rapidapi.com"
H    = {"X-RapidAPI-Key": KEY, "X-RapidAPI-Host": "exercisedb.p.rapidapi.com"}

print("=== DIAGNOSTICO ExerciseDB ===\n")

# 1. Chamada basica sem paginacao
print("--- GET /exercises (limit=10, offset=0) ---")
r = requests.get(f"{BASE}/exercises", headers=H, params={"limit": 10, "offset": 0}, timeout=30)
print("status_code:", r.status_code)

data = r.json()
print("tipo:", type(data).__name__)

if isinstance(data, list):
    print("quantidade retornada:", len(data))
    if data:
        print("chaves do primeiro exercicio:", list(data[0].keys()))
        print()
        print("Primeiros 10 exercicios:")
        for ex in data[:10]:
            gif = str(ex.get("gifUrl", ""))[:70]
            print("  id=" + str(ex.get("id", "?"))
                  + "  bodyPart=" + str(ex.get("bodyPart", "?"))
                  + "  name=" + str(ex.get("name", "?"))[:40])
            print("    gifUrl=" + gif)
else:
    print("resposta nao e lista:")
    print(json.dumps(data, indent=2)[:800])

# 2. Testa limit maior
print("\n--- GET /exercises (limit=1300, offset=0) ---")
r2 = requests.get(f"{BASE}/exercises", headers=H, params={"limit": 1300, "offset": 0}, timeout=30)
print("status_code:", r2.status_code)
if r2.status_code == 200:
    d2 = r2.json()
    print("quantidade com limit=1300:", len(d2) if isinstance(d2, list) else "nao e lista")

# 3. Testa busca por bodyPart
print("\n--- GET /exercises/bodyPartList ---")
r3 = requests.get(f"{BASE}/exercises/bodyPartList", headers=H, timeout=15)
print("status_code:", r3.status_code)
if r3.status_code == 200:
    print("bodyParts disponiveis:", r3.json())
else:
    print("resposta:", r3.text[:200])

# 4. Testa busca por bodyPart especifico
print("\n--- GET /exercises/bodyPart/chest (limit=10) ---")
r4 = requests.get(f"{BASE}/exercises/bodyPart/chest", headers=H,
                  params={"limit": 10, "offset": 0}, timeout=15)
print("status_code:", r4.status_code)
if r4.status_code == 200:
    d4 = r4.json()
    print("quantidade retornada:", len(d4) if isinstance(d4, list) else "nao e lista")