"""Costo de mantenimiento por tipo (ISO/IEC 14764) a partir del historial de commits de VisionCare.

Cada commit sigue Conventional Commits (tipo(alcance): mensaje), así que el tipo del commit se
traduce a un tipo de mantenimiento con las definiciones de la sección 1.2 del módulo:

    fix                      -> Correctivo  (corrige un defecto)
    chore, build, ci         -> Adaptativo  (Docker, build, configuración, dependencias: el entorno)
    feat, perf, docs         -> Perfectivo  (nueva funcionalidad, desempeño o mantenibilidad/documentación)
    refactor, test, style    -> Preventivo  (mejora estructura o red de pruebas antes de que fallen)
    Initial commit, scaffold -> Desarrollo inicial (no es mantenimiento; se excluye)

Como no hay horas registradas, el costo se aproxima con dos indicadores: número de commits y
churn (líneas añadidas + eliminadas). Se compara con la distribución clásica de Lientz y Swanson.

Uso:
    python metricas/costo_mantenimiento.py                      # usa metricas/datos/commits-visioncare.txt
    python metricas/costo_mantenimiento.py --repos ../vc-api ../vc-portal ../vc-db ../vc-infra ../vc-docs
"""
import argparse
import re
import subprocess
from collections import defaultdict
from pathlib import Path

from comun import escribir_csv

RAIZ = Path(__file__).resolve().parent.parent
DATOS = RAIZ / "metricas" / "datos" / "commits-visioncare.txt"
SALIDA = RAIZ / "metricas" / "resultados"

LIENTZ_SWANSON = {"Correctivo": 21, "Adaptativo": 25, "Perfectivo": 50, "Preventivo": 4}
MAPEO = {
    "fix": "Correctivo",
    "chore": "Adaptativo", "build": "Adaptativo", "ci": "Adaptativo",
    "feat": "Perfectivo", "perf": "Perfectivo", "docs": "Perfectivo",
    "refactor": "Preventivo", "test": "Preventivo", "style": "Preventivo",
}
REPOS_DE_CODIGO = {"vc-api", "vc-portal", "vc-db", "vc-infra"}
_TIPO = re.compile(r"^(\w+)(\([^)]*\))?!?:")


def clasificar(mensaje: str) -> str:
    texto = mensaje.lower()
    if texto.startswith("initial commit") or "scaffold" in texto or "start v2.0.0" in texto \
            or re.search(r"create the .* project", texto):
        return "Desarrollo inicial"
    m = _TIPO.match(mensaje)
    return MAPEO.get(m.group(1).lower(), "Sin clasificar") if m else "Sin clasificar"


def leer_snapshot(ruta: Path):
    for linea in ruta.read_text(encoding="utf-8").splitlines():
        repo, sha, fecha, autor, mensaje, ins, dels = linea.split("|")
        yield {"repo": repo, "sha": sha, "fecha": fecha, "autor": autor, "mensaje": mensaje,
               "insertadas": int(ins or 0), "eliminadas": int(dels or 0)}


def leer_repos(rutas):
    for ruta in map(Path, rutas):
        salida = subprocess.run(
            ["git", "-C", str(ruta), "log", "--all", "--no-merges", "--date=short",
             "--pretty=format:@@%h|%ad|%an|%s", "--numstat"],
            capture_output=True, text=True, encoding="utf-8", check=True).stdout
        actual = None
        for linea in salida.splitlines():
            if linea.startswith("@@"):
                if actual:
                    yield actual
                sha, fecha, autor, mensaje = linea[2:].split("|", 3)
                actual = {"repo": ruta.name, "sha": sha, "fecha": fecha, "autor": autor, "mensaje": mensaje,
                          "insertadas": 0, "eliminadas": 0}
            elif linea.strip() and actual:
                a, d, _ = linea.split("\t", 2)
                actual["insertadas"] += int(a) if a.isdigit() else 0
                actual["eliminadas"] += int(d) if d.isdigit() else 0
        if actual:
            yield actual


def sin_duplicados(commits):
    """--all incluye copias rebasadas del mismo cambio en otras ramas: se deja una por (repo, mensaje, fecha)."""
    vistos, unicos = set(), []
    for c in commits:
        clave = (c["repo"], c["mensaje"], c["fecha"])
        if clave not in vistos:
            vistos.add(clave)
            unicos.append(c)
    return unicos


def distribucion(commits):
    tabla = defaultdict(lambda: {"commits": 0, "churn": 0})
    for c in commits:
        tabla[c["tipo"]]["commits"] += 1
        tabla[c["tipo"]]["churn"] += c["insertadas"] + c["eliminadas"]
    return tabla


def tabla_md(titulo, commits):
    mant = [c for c in commits if c["tipo"] in LIENTZ_SWANSON]
    dist = distribucion(mant)
    total_c = sum(d["commits"] for d in dist.values()) or 1
    total_l = sum(d["churn"] for d in dist.values()) or 1
    filas = [f"### {titulo}\n", "| Tipo | Commits | % commits | Churn (lineas) | % churn | Lientz y Swanson |",
             "|---|---|---|---|---|---|"]
    for tipo, ref in LIENTZ_SWANSON.items():
        d = dist.get(tipo, {"commits": 0, "churn": 0})
        filas.append(f"| {tipo} | {d['commits']} | {100 * d['commits'] / total_c:.1f}% | {d['churn']} | "
                     f"{100 * d['churn'] / total_l:.1f}% | {ref}% |")
    return "\n".join(filas) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--repos", nargs="*", help="rutas a los repositorios git de VisionCare")
    args = parser.parse_args()
    commits = list(leer_repos(args.repos) if args.repos else leer_snapshot(DATOS))
    commits = sin_duplicados(commits)
    for c in commits:
        c["tipo"] = clasificar(c["mensaje"])

    escribir_csv(SALIDA / "commits-clasificados.csv", commits,
                 ["repo", "sha", "fecha", "autor", "tipo", "insertadas", "eliminadas", "mensaje"])
    codigo = [c for c in commits if c["repo"] in REPOS_DE_CODIGO]
    excluidos = sum(1 for c in commits if c["tipo"] == "Desarrollo inicial")
    texto = ["# Costo de mantenimiento por tipo - VisionCare\n",
             f"Commits unicos analizados: {len(commits)} (excluidos {excluidos} de desarrollo inicial)\n",
             tabla_md("Todos los repositorios", commits),
             tabla_md("Solo repositorios de codigo (vc-api, vc-portal, vc-db, vc-infra)", codigo)]
    sin_clasificar = [c for c in commits if c["tipo"] == "Sin clasificar"]
    if sin_clasificar:
        texto.append("Commits sin tipo convencional: " + ", ".join(c["sha"] for c in sin_clasificar) + "\n")
    (SALIDA / "costo-mantenimiento.md").write_text("\n".join(texto), encoding="utf-8")
    print("\n".join(texto))


if __name__ == "__main__":
    main()
