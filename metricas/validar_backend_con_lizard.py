"""Valida el medidor Java propio (carpeta test de vc-api) contra una herramienta externa: lizard.

Compara, método por método, V(G), NLOC y número de parámetros. Primero hay que generar
vc-api/target/metricas/metodos.csv con:  cd vc-api && mvn -q test -Dtest=MetricasMantenibilidadTest

Uso:  python metricas/validar_backend_con_lizard.py
"""
import csv
from pathlib import Path

import lizard

RAIZ = Path(__file__).resolve().parent.parent
FUENTES = RAIZ / "vc-api" / "src" / "main" / "java"
PROPIO = RAIZ / "vc-api" / "target" / "metricas" / "metodos.csv"
SALIDA = RAIZ / "metricas" / "resultados" / "validacion-lizard.md"


def main():
    externo = {}
    for ruta in FUENTES.rglob("*.java"):
        codigo = ruta.read_text(encoding="utf-8").replace("\r\n", "\n")
        for f in lizard.analyze_file.analyze_source_code(str(ruta), codigo).function_list:
            externo[(ruta.relative_to(FUENTES).as_posix(), f.start_line)] = (f.cyclomatic_complexity, f.nloc, f.parameter_count)

    propio = {}
    with PROPIO.open(encoding="utf-8") as f:
        for fila in csv.DictReader(f):
            propio[(fila["archivo"], int(fila["linea"]))] = (int(fila["cc"]), int(fila["nloc"]), int(fila["parametros"]))

    comunes = set(externo) & set(propio)
    iguales = [sum(externo[k][i] == propio[k][i] for k in comunes) for i in range(3)]
    diferencias = sorted(k for k in comunes if externo[k] != propio[k])
    texto = [
        "# Validacion del medidor Java contra lizard " + lizard.version + "\n",
        f"- Metodos detectados por lizard: {len(externo)}",
        f"- Metodos detectados por el medidor propio: {len(propio)}",
        f"- Metodos comparables (mismo archivo y linea): {len(comunes)}",
        f"- Coincidencia V(G): {iguales[0]}/{len(comunes)}",
        f"- Coincidencia NLOC: {iguales[1]}/{len(comunes)}",
        f"- Coincidencia parametros: {iguales[2]}/{len(comunes)}",
        f"- Metodos que solo detecta el medidor propio: {len(set(propio) - set(externo))} "
        "(lizard no reconoce las clases anotadas con argumentos, p. ej. @RequestMapping(\"/api/...\") o "
        "@Table(name = ...), ni los metodos dentro de records: omite completos los controladores, las "
        "entidades JPA *Data y los DTO record)",
        f"- Metodos que solo detecta lizard: {len(set(externo) - set(propio))}",
    ]
    if diferencias:
        texto += ["", "Diferencias:"] + [f"- {k}: lizard={externo[k]} propio={propio[k]}" for k in diferencias]
    SALIDA.parent.mkdir(parents=True, exist_ok=True)
    SALIDA.write_text("\n".join(texto) + "\n", encoding="utf-8")
    print("\n".join(texto))


if __name__ == "__main__":
    main()
