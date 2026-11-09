"""Mide vc-portal (React + TypeScript): V(G), NLOC, parámetros, anidamiento, Halstead y MI por función.

Uso (desde la raíz del repositorio):
    pip install -r metricas/requirements.txt
    python metricas/medir_frontend.py            # mide vc-portal/src
    python metricas/medir_frontend.py otra/ruta  # mide otra carpeta

Salida: metricas/resultados/frontend-funciones.csv, frontend-archivos.csv y frontend-resumen.md

Herramienta: lizard (https://github.com/terryyin/lizard) calcula V(G), NLOC y parámetros; este script
añade el Volumen de Halstead y el Índice de Mantenibilidad con las mismas fórmulas del medidor Java.
"""
import sys
from pathlib import Path

import lizard

from comun import clasificar_mi, escribir_csv, indice_mantenibilidad, lineas_loc, riesgo_cc, volumen_halstead

RAIZ = Path(__file__).resolve().parent.parent
SALIDA = RAIZ / "metricas" / "resultados"
UMBRAL_CC, UMBRAL_LOC = 10, 50


def medir(origen: Path):
    funciones, archivos = [], []
    for ruta in sorted(list(origen.rglob("*.ts")) + list(origen.rglob("*.tsx"))):
        if "__tests__" in ruta.parts or ruta.name.endswith(".d.ts"):
            continue
        codigo = ruta.read_text(encoding="utf-8").replace("\r\n", "\n")
        relativo = ruta.relative_to(origen).as_posix()
        archivos.append({"archivo": relativo, **lineas_loc(codigo)})
        lineas = codigo.split("\n")
        lista = lizard.analyze_file.analyze_source_code(str(ruta), codigo).function_list
        for f in lista:
            # Halstead solo sobre las líneas propias: se descuentan las funciones anidadas (handlers,
            # validate, submit...), igual que lizard las descuenta del NLOC y de V(G) del componente.
            anidadas = [g for g in lista if g is not f and f.start_line < g.start_line and g.end_line <= f.end_line]
            propias = [n for n in range(f.start_line, f.end_line + 1)
                       if not any(g.start_line <= n <= g.end_line for g in anidadas)]
            fragmento = "\n".join(lineas[n - 1] for n in propias)
            v = volumen_halstead(fragmento)
            mi = indice_mantenibilidad(v, f.cyclomatic_complexity, f.nloc)
            funciones.append({
                "archivo": relativo, "funcion": f.name, "linea": f.start_line,
                "cc": f.cyclomatic_complexity, "riesgo": riesgo_cc(f.cyclomatic_complexity),
                "nloc": f.nloc, "parametros": f.parameter_count,
                "volumen_halstead": round(v, 1), "mi": round(mi, 1), "mi_nivel": clasificar_mi(mi),
            })
    return funciones, archivos


def resumen(funciones, archivos) -> str:
    total = {k: sum(a[k] for a in archivos) for k in ("fisicas", "efectivas", "comentario", "en_blanco")}
    out = ["# Resumen de metricas - vc-portal\n",
           f"Archivos: {len(archivos)} | Funciones/componentes: {len(funciones)}\n",
           f"LOC fisicas {total['fisicas']} | efectivas {total['efectivas']} | comentario {total['comentario']} "
           f"({100 * total['comentario'] / max(total['fisicas'], 1):.1f}%) | en blanco {total['en_blanco']}\n",
           "## Distribucion de V(G)\n", "| Riesgo | Funciones |", "|---|---|"]
    for nivel in ("Bajo", "Moderado", "Alto", "Muy alto"):
        out.append(f"| {nivel} | {sum(1 for f in funciones if f['riesgo'] == nivel)} |")
    out += ["", "## Funciones que superan V(G) > 10 o NLOC > 50\n",
            "| Funcion | Archivo | V(G) | NLOC | MI |", "|---|---|---|---|---|"]
    for f in sorted(funciones, key=lambda x: (-x["cc"], -x["nloc"])):
        if f["cc"] > UMBRAL_CC or f["nloc"] > UMBRAL_LOC:
            out.append(f"| {f['funcion']} | {f['archivo']} | {f['cc']} | {f['nloc']} | {f['mi']} |")
    return "\n".join(out) + "\n"


if __name__ == "__main__":
    origen = Path(sys.argv[1]) if len(sys.argv) > 1 else RAIZ / "vc-portal" / "src"
    funciones, archivos = medir(origen)
    escribir_csv(SALIDA / "frontend-funciones.csv", funciones, list(funciones[0].keys()))
    escribir_csv(SALIDA / "frontend-archivos.csv", archivos, list(archivos[0].keys()))
    texto = resumen(funciones, archivos)
    (SALIDA / "frontend-resumen.md").write_text(texto, encoding="utf-8")
    print(texto)
