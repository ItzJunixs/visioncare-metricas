# Fichas de métricas (Anexo B)

Se aplican **cinco métricas** sobre VisionCare v1.0.0 (backend `vc-api` en Java 21 / Spring Boot 3.5 y
frontend `vc-portal` en React 18 + TypeScript). Cumplen la regla de selección de la guía: dos son
**estructurales** (V(G) y LOC) y tres evalúan el **diseño o la evolución** (MI, suite CK y costo de
mantenimiento por tipo).

Todas las mediciones son reproducibles con un comando (ver la fila *Herramienta* de cada ficha y la
sección *Cómo medir* del README).

---

## Ficha 1 · Complejidad ciclomática V(G) de McCabe

| Campo | Contenido |
|---|---|
| **Nombre** | Complejidad ciclomática V(G) (McCabe, 1976) |
| **Atributo que mide** | Complejidad lógica: número de caminos linealmente independientes de un método o función; indica cuántos casos de prueba mínimos necesita. |
| **Fórmula** | Formal: `V(G) = E − N + 2P`. Práctica (la del módulo): `V(G) = puntos de decisión + 1`, contando `if`, `for`, `while`, `case`, `catch`, `&&`, `\|\|` y el ternario `?:`. En TSX, lizard también cuenta `??`, y el renderizado condicional `{x && <Componente/>}` suma porque es una rama real de la interfaz. `else` y `default` no suman. |
| **Umbral e interpretación** | 1–10 bajo · 11–20 moderado · 21–50 alto · > 50 muy alto (tabla 2.3 del módulo y tabla de la guía). Se propone como **RNF-03: ningún método o función supera V(G) = 10**. |
| **Herramienta** | Backend: medidor propio `vc-api/src/test/java/co/visioncare/vcapi/metricas/ComplejidadCiclomatica.java`, ejecutado con `mvn -q test -Dtest=MetricasMantenibilidadTest`. Validado contra **lizard 1.25.1**: coincide en 291 de 291 métodos comparables (`python metricas/validar_backend_con_lizard.py`). Frontend: lizard vía `python metricas/medir_frontend.py`. |
| **Alcance** | Todo `vc-api/src/main/java` (146 archivos, 419 métodos y constructores) y todo `vc-portal/src` sin pruebas (23 archivos, 193 funciones y componentes). |
| **Justificación** | Los requisitos de VisionCare se concentran en reglas de negocio (BR-02 eje 0–180, BR-08 secuencia de estados, BR-09 descuento de stock, BR-10 cancelación con motivo). Un método con muchas ramas es donde esas reglas se rompen al cambiar el código, y V(G) dice cuántas pruebas hacen falta para cubrirlo. |
| **Acción si se supera** | Extract Method / Decompose Conditional; en React, Extract Component y Extract Custom Hook. |

## Ficha 2 · Líneas de código (LOC) y porcentaje de comentarios

| Campo | Contenido |
|---|---|
| **Nombre** | LOC físicas, efectivas (SLOC/NLOC), de comentario y en blanco; % de comentarios |
| **Atributo que mide** | Tamaño del código y proporción de documentación interna. |
| **Fórmula** | Físicas = todas las líneas. Efectivas = líneas con al menos un token de código. Comentario = líneas con solo comentario (una línea `x = 1; // nota` cuenta como efectiva). En blanco = líneas vacías. `% comentarios = comentario / físicas × 100`. NLOC por método = líneas efectivas entre la firma y la llave de cierre. |
| **Umbral e interpretación** | Más de **50 líneas efectivas por método** es señal de alerta (sección 2.1 y guía). En React el "método" es el componente completo, JSX incluido, así que el umbral mide el tamaño del componente. |
| **Herramienta** | Backend: `MetricasLOC.java` (mismo test). Corrige el caso del ejemplo de clase en que una línea con código y comentario final se contaba mal. Se calibró con el fragmento `sumar()` del módulo (4 físicas, 3 efectivas, 1 comentario). Frontend: `metricas/comun.py → lineas_loc`, que también reconoce `{/* */}` en JSX. |
| **Alcance** | Igual que la ficha 1, por archivo y por método. |
| **Justificación** | NFR-08 de VisionCare pide que un desarrollador nuevo levante y entienda el sistema en menos de 1 hora. El tamaño y la documentación del código afectan directamente esa curva de entrada. |
| **Acción si se supera** | Extract Method / Extract Component. Si el % de comentarios es bajo, documentar las clases de dominio y los componentes principales (la guía lo pide en la fase 3). |

## Ficha 3 · Índice de Mantenibilidad (MI)

| Campo | Contenido |
|---|---|
| **Nombre** | Índice de Mantenibilidad (Oman y Hagemeister) |
| **Atributo que mide** | Mantenibilidad global: combina tamaño léxico (Halstead), complejidad y LOC en un solo indicador. |
| **Fórmula** | `MI = 171 − 5.2·ln(HV) − 0.23·CC − 16.2·ln(LOC)`, con `HV = N·log2(n)` (volumen de Halstead). Operadores = palabras clave y símbolos. Operandos = identificadores y literales. Por clase se usan los promedios por método (variante del SEI). También se informa la versión normalizada 0–100 de Visual Studio: `max(0, MI·100/171)`. |
| **Umbral e interpretación** | Escala original: ≥ 85 alta, 65–85 moderada, **< 65 baja** (guía: "cercano a 100 alta, menor de 65 baja"). En métodos muy pequeños la escala original supera 100: no es un error de cálculo, solo indica que el método es trivial. |
| **Herramienta** | Backend: `Halstead.java` + `IndiceMantenibilidad.java`. Frontend: `metricas/comun.py` (mismas fórmulas). En el frontend, el volumen de Halstead de un componente descuenta las funciones anidadas, igual que lizard las descuenta del NLOC y de V(G). |
| **Alcance** | Cada método o función, y agregado por clase y por módulo. |
| **Justificación** | V(G) y LOC pueden dar resultados distintos; por ejemplo, un componente largo pero lineal tiene V(G) baja y LOC alta. El MI resume ambas y permite comparar módulos y backend contra frontend con un solo número. |
| **Acción si se supera** | Se actúa sobre el término dominante: si pesa LOC, Extract Method/Component; si pesa V(G), Decompose Conditional. |

## Ficha 4 · Suite CK (Chidamber y Kemerer)

| Campo | Contenido |
|---|---|
| **Nombre** | WMC, DIT, NOC, CBO, RFC, LCOM (con LCOM4 como confirmación) |
| **Atributo que mide** | Calidad del diseño orientado a objetos: complejidad ponderada, herencia, acoplamiento, tamaño de la respuesta y cohesión. |
| **Fórmula** | **WMC** = Σ V(G) de los métodos de la clase. **DIT** = profundidad en la jerarquía (Object = 1; para padres del JDK/Spring se usa su profundidad real, p. ej. `RuntimeException` = 4). **NOC** = subclases directas. **CBO** = tipos distintos usados por la clase, sin `java.*` (mismo criterio que la herramienta *ck* de Aniche). **RFC** = métodos propios + métodos distintos invocados. **LCOM\*** (Henderson-Sellers, escala 0–1) = `(prom μ(Aj) − m)/(1 − m)`. **LCOM4** = número de grupos de métodos conectados por atributos compartidos o llamadas, sin contar accesores. |
| **Umbral e interpretación** | WMC > 20, DIT > 5, CBO > 10, RFC > 50, LCOM > 0.7 (tabla de la guía). **Criterio del equipo para LCOM**: una clase se marca solo si LCOM\* > 0.7 **y** LCOM4 > 1. Sin esta confirmación, cualquier entidad con getters sale como "no cohesiva" (ver resultados). God Class = WMC > 20 y CBO > 10 y LCOM confirmado a la vez. |
| **Herramienta** | `SuiteCK.java`, `ClaseCK.java` y `RecomendadorRefactorizacion.java` (mismo test), basados en los ejemplos `ClaseCK`/`SuiteCK` del módulo. Salida: `clases-ck.csv`. |
| **Alcance** | Las 154 clases, interfaces, enums y records de `vc-api`. No aplica a `vc-portal`: es React con componentes funcionales, no hay clases (regla de la guía: "si el lenguaje no es OO, la suite CK no aplica"). |
| **Justificación** | VisionCare es un monolito modular cuyo objetivo arquitectónico declarado (ADR-001, `module-boundaries.md`) es **bajo acoplamiento entre módulos y alta cohesión dentro de cada uno**. CBO y LCOM verifican ese objetivo con números. |
| **Acción si se supera** | WMC/LCOM → Extract Class / Move Method. CBO → Facade o dividir la clase. DIT → aplanar la jerarquía o Replace Inheritance with Delegation. Long Parameter List (dato complementario) → Introduce Parameter Object. |

## Ficha 5 · Costo de mantenimiento por tipo

| Campo | Contenido |
|---|---|
| **Nombre** | Distribución del esfuerzo de mantenimiento por tipo (ISO/IEC 14764) |
| **Atributo que mide** | Evolución del sistema: en qué tipo de cambio se invierte el esfuerzo. |
| **Fórmula** | Cada commit se clasifica por su tipo de Conventional Commits: `fix` → correctivo; `chore/build/ci` → adaptativo; `feat/perf/docs` → perfectivo; `refactor/test/style` → preventivo. Los commits iniciales o de *scaffold* no son mantenimiento y se excluyen. Como no hay horas registradas, el costo se aproxima por **número de commits** y por **churn** (líneas añadidas + eliminadas). `% tipo = commits del tipo / commits de mantenimiento × 100`. |
| **Umbral e interpretación** | Se compara contra Lientz y Swanson (1980): correctivo 21 %, adaptativo 25 %, perfectivo 50 %, preventivo 4 %. |
| **Herramienta** | `python metricas/costo_mantenimiento.py` (usa la foto del historial en `metricas/datos/commits-visioncare.txt`) o `--repos <rutas>` para leer los 5 repositorios con `git log`. |
| **Alcance** | 87 commits únicos de vc-api, vc-portal, vc-db, vc-infra y vc-docs (se eliminan copias rebasadas en otras ramas). Se reporta todo junto y también solo los repositorios de código. |
| **Justificación** | Es la única métrica del catálogo que mira la **evolución** y no solo el estado actual. Además, sirve de línea base para la recomendación de la guía de etiquetar issues por tipo. |
| **Acción** | Registrar horas por issue etiquetado y subir la proporción de mantenimiento preventivo (pruebas y refactorizaciones). |
