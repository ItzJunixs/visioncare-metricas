"""Funciones compartidas por los scripts de medición (Halstead, Índice de Mantenibilidad, CSV)."""
import csv
import math
import re
from pathlib import Path

# Tokenizador para TypeScript/TSX: cadenas, plantillas, números, identificadores y operadores.
_TOKEN = re.compile(
    r"""
    (?P<str>"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|`(?:\\.|[^`\\])*`)
  | (?P<num>\b\d[\d_]*(?:\.\d+)?\b)
  | (?P<id>[A-Za-z_$][\w$]*)
  | (?P<op>===|!==|\?\?=|\?\?|\?\.|=>|\.\.\.|&&|\|\||==|!=|<=|>=|\+\+|--|[+\-*/%]=|[{}()\[\];,.<>+\-*/%=!?:&|^~@])
    """,
    re.VERBOSE,
)
_COMENTARIOS = re.compile(r"//[^\n]*|/\*.*?\*/", re.DOTALL)

PALABRAS_CLAVE_TS = {
    "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue", "return",
    "function", "const", "let", "var", "new", "try", "catch", "finally", "throw", "typeof", "instanceof",
    "in", "of", "async", "await", "import", "export", "from", "class", "extends", "interface", "type",
    "as", "void", "delete",
}
OPERANDOS_CLAVE = {"true", "false", "null", "undefined", "this"}
CIERRES = {")", "]", "}"}


def quitar_comentarios(codigo: str) -> str:
    """Reemplaza comentarios por espacios conservando los saltos de línea."""
    return _COMENTARIOS.sub(lambda m: re.sub(r"[^\n]", " ", m.group(0)), codigo)


def volumen_halstead(codigo: str) -> float:
    """V = N · log2(n): operadores = palabras clave y símbolos; operandos = identificadores y literales."""
    operadores, operandos = set(), set()
    n_op = n_opnd = 0
    for m in _TOKEN.finditer(quitar_comentarios(codigo)):
        texto = m.group(0)
        if m.lastgroup in ("str", "num") or (m.lastgroup == "id" and texto not in PALABRAS_CLAVE_TS) or texto in OPERANDOS_CLAVE:
            operandos.add(texto)
            n_opnd += 1
        elif texto not in CIERRES:
            operadores.add(texto)
            n_op += 1
    vocabulario = len(operadores) + len(operandos)
    return 0.0 if vocabulario <= 1 else (n_op + n_opnd) * math.log2(vocabulario)


def indice_mantenibilidad(volumen: float, cc: float, loc: float) -> float:
    """MI = 171 - 5.2·ln(HV) - 0.23·CC - 16.2·ln(LOC)  (Oman y Hagemeister, sección 2.4 del módulo)."""
    return 171 - 5.2 * math.log(max(volumen, 1)) - 0.23 * cc - 16.2 * math.log(max(loc, 1))


def clasificar_mi(mi: float) -> str:
    return "Alta" if mi >= 85 else "Moderada" if mi >= 65 else "Baja"


def riesgo_cc(cc: int) -> str:
    return "Bajo" if cc <= 10 else "Moderado" if cc <= 20 else "Alto" if cc <= 50 else "Muy alto"


def lineas_loc(codigo: str) -> dict:
    """LOC físicas, efectivas, de comentario y en blanco de un archivo TS/TSX (incluye {/* */} de JSX)."""
    sin_comentarios = quitar_comentarios(codigo).split("\n")
    original = codigo.split("\n")
    if original and original[-1] == "":
        original, sin_comentarios = original[:-1], sin_comentarios[:-1]
    efectivas = comentario = blanco = 0
    for orig, limpio in zip(original, sin_comentarios):
        if limpio.strip() and limpio.strip() not in ("{}",):
            efectivas += 1
        elif orig.strip():
            comentario += 1
        else:
            blanco += 1
    return {"fisicas": len(original), "efectivas": efectivas, "comentario": comentario, "en_blanco": blanco}


def escribir_csv(ruta: Path, filas: list, columnas: list) -> None:
    ruta.parent.mkdir(parents=True, exist_ok=True)
    with ruta.open("w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=columnas)
        w.writeheader()
        for fila in filas:
            w.writerow({c: fila.get(c, "") for c in columnas})
