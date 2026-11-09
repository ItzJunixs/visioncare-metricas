"""Genera los gráficos del informe a partir de los CSV de resultados.

Requiere haber ejecutado antes el medidor Java (vc-api/target/metricas) y los scripts
medir_frontend.py y costo_mantenimiento.py.

Uso:  python metricas/graficos.py      -> docs/metricas/graficos/*.png
"""
import csv
from collections import Counter
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

RAIZ = Path(__file__).resolve().parent.parent
BACK = RAIZ / "vc-api" / "target" / "metricas"
RES = RAIZ / "metricas" / "resultados"
OUT = RAIZ / "docs" / "metricas" / "graficos"

AZUL, NARANJA = "#2a78d6", "#eb6834"          # serie 1 y 2 de la paleta categórica
TINTA, TINTA_2, REJILLA = "#0b0b0b", "#52514e", "#e4e3df"
UMBRAL = "#52514e"

plt.rcParams.update({
    "font.size": 10, "axes.edgecolor": REJILLA, "axes.labelcolor": TINTA_2, "xtick.color": TINTA_2,
    "ytick.color": TINTA_2, "axes.spines.top": False, "axes.spines.right": False, "axes.grid": True,
    "grid.color": REJILLA, "grid.linewidth": 0.6, "axes.axisbelow": True, "figure.dpi": 150,
    "axes.titleweight": "bold", "axes.titlesize": 11, "axes.titlecolor": TINTA,
})


def leer(ruta):
    with open(ruta, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def umbral(ax, valor, texto, horizontal=False):
    if horizontal:
        estilo = "--" if "alta" not in texto else ":"
        ax.axvline(valor, color=UMBRAL, linestyle=estilo, linewidth=1, label=f"umbral {texto}".replace("umbral umbral", "umbral"))
        ax.legend(frameon=False, loc="lower right")
    else:
        ax.axhline(valor, color=UMBRAL, linestyle="--", linewidth=1)
        ax.text(ax.get_xlim()[1], valor, f" {texto}", color=UMBRAL, va="center", fontsize=8)


def guardar(fig, nombre):
    OUT.mkdir(parents=True, exist_ok=True)
    fig.tight_layout()
    fig.savefig(OUT / nombre, bbox_inches="tight")
    plt.close(fig)


def distribucion_cc(back, front):
    rangos = ["1", "2", "3", "4", "5-10", "11-20", "21-50"]

    def rango(cc):
        return str(cc) if cc <= 4 else "5-10" if cc <= 10 else "11-20" if cc <= 20 else "21-50"

    cb = Counter(rango(int(m["cc"])) for m in back)
    cf = Counter(rango(int(f["cc"])) for f in front)
    tb = [100 * cb[r] / len(back) for r in rangos]
    tf = [100 * cf[r] / len(front) for r in rangos]
    fig, ax = plt.subplots(figsize=(7.5, 3.6))
    x = range(len(rangos))
    ax.bar([i - 0.2 for i in x], tb, width=0.38, color=AZUL, label=f"vc-api (Java, {len(back)} métodos)")
    ax.bar([i + 0.2 for i in x], tf, width=0.38, color=NARANJA, label=f"vc-portal (TSX, {len(front)} funciones)")
    for i, r in enumerate(rangos):
        if cf[r] and i >= 5:
            ax.text(i + 0.2, tf[i] + 1, str(cf[r]), ha="center", fontsize=8, color=TINTA_2)
    ax.set_xticks(list(x), rangos)
    ax.set_xlabel("V(G) por método / función")
    ax.set_ylabel("% de métodos")
    ax.axvline(4.5, color=UMBRAL, linestyle="--", linewidth=1)
    ax.text(4.55, max(tb) * 0.9, "umbral 10", color=UMBRAL, fontsize=8)
    ax.set_title("Distribución de la complejidad ciclomática")
    ax.legend(frameon=False)
    guardar(fig, "01-distribucion-cc.png")


def top_frontend(front):
    top = sorted(front, key=lambda f: -int(f["cc"]))[:10][::-1]
    fig, ax = plt.subplots(figsize=(7.5, 3.8))
    nombres = [f"{f['funcion']} ({Path(f['archivo']).stem})" if f["funcion"] in ("validate", "submit", "create")
               else f["funcion"] for f in top]
    ax.barh(nombres, [int(f["cc"]) for f in top], color=NARANJA, height=0.6)
    for i, f in enumerate(top):
        ax.text(int(f["cc"]) + 0.3, i, f"{f['cc']}  ·  {f['nloc']} NLOC", va="center", fontsize=8, color=TINTA_2)
    ax.set_xlim(0, 30)
    umbral(ax, 10, "V(G) = 10", horizontal=True)
    ax.set_xlabel("V(G)")
    ax.set_title("vc-portal: las 10 funciones más complejas")
    ax.grid(axis="y", visible=False)
    guardar(fig, "02-top-cc-frontend.png")


def ck_barras(clases, campo, limite, titulo, archivo, n=12):
    top = sorted(clases, key=lambda c: -float(c[campo]))[:n][::-1]
    fig, ax = plt.subplots(figsize=(7.5, 3.8))
    ax.barh([c["clase"] for c in top], [float(c[campo]) for c in top], color=AZUL, height=0.6)
    for i, c in enumerate(top):
        ax.text(float(c[campo]) + 0.2, i, c[campo], va="center", fontsize=8, color=TINTA_2)
    umbral(ax, limite, f"umbral {limite}", horizontal=True)
    ax.set_title(titulo)
    ax.grid(axis="y", visible=False)
    guardar(fig, archivo)


def lcom(clases):
    altas = [c for c in clases if float(c["lcom"]) > 0.7]
    confirmadas = [c for c in altas if int(c["lcom4"]) > 1]
    datos = [("LCOM* > 0.7", len(altas)), ("…y LCOM4 > 1\n(confirmadas)", len(confirmadas))]
    fig, ax = plt.subplots(figsize=(5.5, 2.6))
    ax.barh([d[0] for d in datos][::-1], [d[1] for d in datos][::-1], color=AZUL, height=0.5)
    for i, d in enumerate(datos[::-1]):
        ax.text(d[1] + 0.3, i, str(d[1]), va="center", fontsize=9, color=TINTA)
    ax.set_xlabel("Clases de vc-api")
    ax.set_title("Falta de cohesión: LCOM* vs. confirmación con LCOM4")
    ax.grid(axis="y", visible=False)
    guardar(fig, "05-lcom-confirmacion.png")


def mi_por_area(back_metodos, front):
    grupos = {}
    for m in back_metodos:
        grupos.setdefault("api · " + m["modulo"], []).append(float(m["mi"]))
    for f in front:
        area = "portal · " + (f["archivo"].split("/")[0] if "/" in f["archivo"] else "núcleo")
        grupos.setdefault(area, []).append(float(f["mi"]))
    filas = sorted(((k, min(v), sum(v) / len(v)) for k, v in grupos.items() if not k.endswith("(raiz)")),
                   key=lambda t: t[1])
    fig, ax = plt.subplots(figsize=(7.5, 3.8))
    nombres = [f[0] for f in filas][::-1]
    ax.barh(nombres, [f[2] for f in filas][::-1], color=[NARANJA if n.startswith("portal") else AZUL for n in nombres],
            height=0.6, label="promedio")
    ax.scatter([f[1] for f in filas][::-1], nombres, color=TINTA, s=18, zorder=3, label="mínimo")
    umbral(ax, 65, "65 (baja)", horizontal=True)
    umbral(ax, 85, "85 (alta)", horizontal=True)
    ax.set_xlabel("Índice de Mantenibilidad (escala original)")
    ax.set_title("MI por módulo: promedio (barra) y peor función (punto)")
    ax.legend(frameon=False, loc="upper center", bbox_to_anchor=(0.5, -0.16), ncol=4)
    ax.grid(axis="y", visible=False)
    guardar(fig, "06-mi-por-modulo.png")


def costo(commits):
    tipos = ["Correctivo", "Adaptativo", "Perfectivo", "Preventivo"]
    referencia = [21, 25, 50, 4]
    mant = [c for c in commits if c["tipo"] in tipos and c["repo"] != "vc-docs"]
    cuenta = Counter(c["tipo"] for c in mant)
    reales = [100 * cuenta[t] / len(mant) for t in tipos]
    fig, ax = plt.subplots(figsize=(7.0, 3.4))
    x = range(len(tipos))
    ax.bar([i - 0.2 for i in x], reales, width=0.38, color=AZUL, label="VisionCare (commits de código)")
    ax.bar([i + 0.2 for i in x], referencia, width=0.38, color=NARANJA, label="Lientz y Swanson (1980)")
    for i in x:
        ax.text(i - 0.2, reales[i] + 1, f"{reales[i]:.0f}%", ha="center", fontsize=8, color=TINTA_2)
        ax.text(i + 0.2, referencia[i] + 1, f"{referencia[i]}%", ha="center", fontsize=8, color=TINTA_2)
    ax.set_xticks(list(x), tipos)
    ax.set_ylabel("% del esfuerzo")
    ax.set_title("Distribución del mantenimiento por tipo")
    ax.legend(frameon=False)
    guardar(fig, "07-costo-mantenimiento.png")


if __name__ == "__main__":
    back = leer(BACK / "metodos.csv")
    clases = leer(BACK / "clases-ck.csv")
    front = leer(RES / "frontend-funciones.csv")
    distribucion_cc(back, front)
    top_frontend(front)
    ck_barras(clases, "cbo", 10, "vc-api: acoplamiento CBO por clase (top 12)", "03-cbo.png")
    ck_barras(clases, "wmc", 20, "vc-api: WMC por clase (top 12)", "04-wmc.png")
    lcom(clases)
    mi_por_area(back, front)
    costo(leer(RES / "commits-clasificados.csv"))
    print("Graficos en", OUT)
