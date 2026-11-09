# Análisis de resultados y plan de mejoras

> Alcance acordado: este proyecto **mide y propone**. No se modifica el código de VisionCare. Las únicas
> adiciones al repositorio son los medidores (carpeta de pruebas de `vc-api` y `metricas/`) y la
> documentación. Cada propuesta indica la técnica, el riesgo, el esfuerzo y el efecto esperado sobre la
> métrica, para que se pueda ejecutar después siguiendo el ciclo **Medir → Detectar → Refactorizar →
> Verificar**.

## 1. Interpretación frente a los umbrales

### 1.1 El backend cumple los umbrales estructurales; el frontend no

- **vc-api:** 419 métodos, todos con V(G) ≤ 4 y NLOC ≤ 26, y ninguno con MI menor de 80. Es
  consecuencia directa de la arquitectura elegida (ADR-001): cada caso de uso es una clase pequeña con un
  único método `execute`, y las reglas de negocio viven en entidades con métodos cortos (`WorkOrder.cancel`,
  `Frame.deductStock`). **El RNF-03 propuesto (V(G) ≤ 10) se cumple en el backend y queda verificado
  automáticamente** por la prueba `rnf03ComplejidadCiclomaticaMaxima`.
- **vc-portal:** 7 funciones superan V(G) = 10 y 10 componentes superan 50 NLOC con MI < 65. La causa es
  la misma en todos: **cada pantalla es un único componente que hace todo**. Carga datos, guarda el estado
  del formulario, valida, llama a la API, maneja errores y dibuja varias secciones. En `PatientDetail` hay
  dos responsabilidades de negocio distintas en el mismo componente (ficha del paciente y CRUD de fórmulas
  ópticas), y por eso tiene V(G) 22, 139 NLOC y MI 37,6, el peor valor del sistema.

**Conclusión:** el frontend no recibió la misma disciplina de separación de responsabilidades que el
backend. Es ahí donde el próximo cambio funcional (por ejemplo, HU-11, el portal del paciente, que no está
en el MVP) va a costar más.

### 1.2 Duplicación en el frontend (detectada al leer los puntos calientes)

- El bloque `setBusy(true); try { await api.x(); toast(ok); reload(); } catch (e) { toast(e instanceof
  ApiError ? e.message : "...", "err"); } finally { setBusy(false); }` se repite **8 veces** en 3
  pantallas (`PatientDetail`, `WorkOrderDetail`, `Inventory`). Cada copia suma +1 de V(G) por el ternario.
- Las validaciones de reglas de negocio se escriben a mano en cada formulario con cadenas de `if`. En
  `FrameNew.validate` hay 7 `if` con 7 operadores `&&`/`||` (V(G) 15). Además, BR-02 (eje 0–180) y BR-04 (venta ≥ compra) se
  validan en el frontend y también en las entidades del backend: si la regla cambia, hay que cambiarla en
  dos lugares.

### 1.3 Suite CK: lo que es un problema real y lo que no

| Hallazgo | ¿Problema real? | Razonamiento |
|---|---|---|
| **`PatientController`**: CBO 19, LCOM4 = 9, constructor con 9 parámetros | **Sí** | Expone dos recursos REST (`/patients` y `/patients/{id}/optical-formulas`) con 9 casos de uso inyectados. Es la clase más acoplada del sistema y la única que concentra dos subdominios. Un cambio en fórmulas obliga a tocar y volver a probar el controlador de pacientes. |
| `FrameController` (CBO 12), `WorkOrderController` (CBO 11) | Moderado | Un recurso cada uno, pero con un caso de uso por endpoint. Superan el umbral por poco. |
| LCOM\* alto en controladores (LCOM4 3–9) | Parcial | En el estilo "un caso de uso por endpoint", cada método del controlador usa un atributo distinto. LCOM4 > 1 es estructural en ese diseño. Solo es grave cuando, además, los grupos pertenecen a recursos distintos (el caso de `PatientController`). |
| **21 clases con LCOM\* > 0,7, pero solo 7 con LCOM4 > 1** | **No** para las otras 14 | Entidades (`Frame`, `WorkOrder`, `Invoice`, `Patient`) y clases JPA `*Data` tienen un accesor por atributo; cada accesor toca un solo atributo, y la fórmula de Henderson-Sellers lo interpreta como falta de cohesión. Al quitar los accesores y medir grupos conectados (LCOM4), 14 de esas clases resultan cohesivas (LCOM4 ≤ 1). **Aplicar el umbral 0,7 sin esta verificación habría llevado a proponer 14 refactorizaciones innecesarias.** |
| `Frame` WMC 26, `WorkOrder` 22, `Invoice` 21, `OpticalFormula` 21 | Leve | El WMC alto viene de muchos métodos simples (V(G) promedio ≈ 1,2), no de métodos complejos. `WorkOrder` e `Invoice` son cohesivas (LCOM4 = 1): no se actúa. `Frame` y `OpticalFormula` sí tienen un problema de diseño concreto (ver P4 y P5). |
| `OrdenesConfig`, `PacientesConfig`, `SecurityConfig` CBO 11 | **No (excepción justificada)** | Son la *raíz de composición* de cada módulo: su único trabajo es conectar casos de uso con repositorios. Bajarles el CBO exigiría anotar los casos de uso con `@Service`, lo que acoplaría la capa de servicio a Spring, en contra de `layered-architecture.md`. Se documenta la excepción y no se cambia. |
| 9 excepciones con DIT 6 | **No (falso positivo útil)** | 4 de los 6 niveles son del JDK (`Object → Throwable → Exception → RuntimeException`). El proyecto aporta solo 2 niveles (`ConflictException → OverpaymentException`), y esa jerarquía es intencional: `ApiExceptionHandler` traduce a HTTP por el tipo base (409, 422, 404, 401). Aplanarla multiplicaría los manejadores. |
| RFC máx. 46 (`OpticalFormulaPersistenceAdapter`), NOC máx. 4 | No | Dentro de los umbrales. |
| **24 métodos con más de 5 parámetros** (hasta 12) | **Sí** | *Long Parameter List* y *Data Clump*: `odSph, odCyl, odAxis, oiSph, oiCyl, oiAxis` viajan juntos por 6 firmas. `rehydrate(...)` de 8 parámetros en cada agregado. |

### 1.4 Costo de mantenimiento por tipo

| Tipo | VisionCare (código) | Lientz y Swanson | Interpretación |
|---|---|---|---|
| Correctivo | 7,9 % | 21 % | Bajo porque **el sistema no ha estado en operación**: los 3 `fix` se encontraron en pruebas, no en producción. Se espera que suba al desplegar. |
| Adaptativo | 26,3 % | 25 % | Similar a la referencia: Docker, Compose, `.gitignore`, `package-lock`, Dockerfiles. |
| Perfectivo | 57,9 % | 50 % | Por encima de la referencia, como es normal en un sistema joven que todavía agrega funcionalidad (`feat`). |
| Preventivo | 7,9 % | 4 % | Por encima de la referencia en commits, pero el churn está inflado por la colección de Postman (1 537 líneas en un commit `test`). En pruebas reales solo hay 3 casos de uso con pruebas unitarias, de 25. |

**Limitación declarada:** esta métrica mide la *evolución* y VisionCare aún no tiene fase de mantenimiento.
Además, el historial de `vc-api` se consolidó en 11 commits el mismo día (2026-11-08), así que el número
de commits refleja cómo se organizó la entrega, no las horas reales. Por eso la recomendación P7 es empezar
a registrar horas en issues etiquetados.

### 1.5 Pruebas: el riesgo que más condiciona las mejoras

`vc-api` tiene 14 pruebas unitarias más 7 reglas ArchUnit, y cubren 3 de 25 casos de uso (`inventario` y
la aprobación de órdenes). **`facturacion`, `pacientes` y `usuarios` no tienen pruebas unitarias.**
`vc-portal` tiene 8 pruebas (cliente API y guardia de rutas) y **ninguna de pantallas**. El NFR-07 de
VisionCare exige ≥ 80 % de cobertura en archivos cambiados y ≥ 90 % en `ordenes` y `facturacion`. Ese
requisito hoy no se puede verificar porque no hay herramienta de cobertura (JaCoCo) configurada.

Según la definición de Fowler, refactorizar es cambiar la estructura **sin alterar el comportamiento
observable**. Sin pruebas no hay forma de demostrar que el comportamiento se preservó, así que **P6 es
requisito previo de P1 y P2**.

---

## 2. Plan de mejoras priorizado

Prioridad = riesgo del problema × beneficio ÷ esfuerzo. Esfuerzo en horas-persona estimadas por el equipo.
Los valores "después" son **estimaciones** que se deben confirmar volviendo a medir (fase *Verificar*).

| # | Problema (code smell) | Elemento | Técnica | Riesgo del problema | Esfuerzo | Métrica: antes → esperado |
|---|---|---|---|---|---|---|
| **P6** | Sin red de pruebas para refactorizar | `facturacion`, `pacientes`, pantallas del portal | Pruebas de caracterización + JaCoCo en el `pom.xml` | **Alto** | 10–12 h | Casos de uso con prueba 3/25 → 25/25; cobertura: no medible → medible (NFR-07) |
| **P1** | Componente Dios / Long Method | `PatientDetail`, `WorkOrderDetail`, `WorkOrderNew` | Extract Component + Extract Custom Hook | **Alto** | 8–10 h | V(G) máx. 22 → ≤ 10; NLOC máx. 139 → ≤ 50; MI 37,6 → > 65 |
| **P2** | Alto acoplamiento, dos recursos en una clase | `PatientController` | Extract Class (`OpticalFormulaController`) | Medio | 2 h | CBO 19 → ≈ 12 y ≈ 9; parámetros del constructor 9 → 5 y 4 |
| **P3** | Código duplicado y condicionales largos | 8 bloques try/catch/toast, `FrameNew.validate` | Extract Function (`useApiAction`) + Replace Conditional with tabla de reglas | Medio | 3–4 h | V(G) `FrameNew.validate` 15 → ≤ 4; `submit`/`create` 11–12 → ≤ 5; −8 copias |
| **P4** | Data Clump / Long Parameter List | `OpticalFormula` (12 parámetros) | Introduce Parameter Object `EyePrescription(sph, cyl, axis)` | Medio | 3 h | Parámetros 12 → 8; WMC 21 → ≈ 18; BR-02 validada en un solo lugar |
| **P5** | Clase con muchas responsabilidades | `Frame` (WMC 26), `Frame.register` 7 parámetros | Extract Class: objetos de valor `Pricing` (BR-04) y `StockLevel` (BR-06, alerta) | Bajo | 3 h | WMC 26 → ≈ 16; parámetros 7 → 5 |
| **P7** | Esfuerzo de mantenimiento no medible | Proceso del equipo | Issues etiquetados (`correctivo`, `adaptativo`, `perfectivo`, `preventivo`) con horas | Bajo | 1 h | Costo por tipo: proxy por commits → horas reales |
| **P8** | Métricas que se degradan sin aviso | Pipeline | Ejecutar `MetricasMantenibilidadTest` y `medir_frontend.py` en CI; fallar si V(G) > 10 | Bajo | 1–2 h | Prevención de regresiones en todas las métricas |

**No se proponen cambios** (con justificación en 1.3) para: configuraciones `*Config` (CBO 11), jerarquía
de excepciones (DIT 6), `WorkOrder` e `Invoice` (WMC 21–22 con LCOM4 = 1) y las 14 clases con LCOM\* alto
no confirmado.

Orden de ejecución recomendado: **P6 → P1 → P3 → P2 → P4 → P5**, con P7 y P8 en paralelo desde el inicio.

---

## 3. Detalle de las propuestas principales

### P1 · Dividir las pantallas del portal (Extract Component + Extract Custom Hook)

Diseño propuesto para `PatientDetail.tsx` (boceto, **no aplicado**):

```
screens/PatientDetail.tsx            -> solo compone; V(G) esperado ≈ 4
├── components/PatientHeaderCard.tsx -> nombre, documento, estado, botón Desactivar
├── components/CurrentFormulaCard.tsx-> fórmula vigente y control periódico (HU-04)
├── components/FormulaHistoryTable.tsx -> historial con Editar / Eliminar
├── components/FormulaForm.tsx       -> el formulario OD/OI (también lo puede usar WorkOrderNew)
└── hooks/useFormulaForm.ts          -> estado del formulario, validate(), toPayload(), submit()
```

```tsx
// Boceto: el componente de pantalla solo orquesta
export function PatientDetail() {
  const { id = "" } = useParams();
  const patient = useAsync(() => api.patient(id), [id]);
  const formulas = useAsync(() => api.formulas(id), [id]);
  const formulaForm = useFormulaForm(id, () => { patient.reload(); formulas.reload(); });
  if (patient.loading) return <Loading />;
  if (patient.error) return <ErrorAlert message={patient.error} />;
  return (
    <Page>
      <PatientHeaderCard patient={patient.data!} onDeactivated={patient.reload} />
      <CurrentFormulaCard patient={patient.data!} onNew={formulaForm.openNew} />
      {formulaForm.open && <FormulaForm {...formulaForm} />}
      <FormulaHistoryTable formulas={formulas.data ?? []} onEdit={formulaForm.openEdit} />
    </Page>
  );
}
```

`WorkOrderDetail` se divide igual: `StatusTimeline` + `WorkOrderActions` + `useWorkOrderActions`
(approve, advance y cancel, hoy tres funciones casi idénticas). `WorkOrderNew` pasa a tener un componente
por paso del asistente (`StepPatient`, `StepFrame`, `StepSummary`).
**Verificación:** volver a correr `medir_frontend.py` y comprobar que ninguna función del portal supere
V(G) 10 ni NLOC 50. Las pruebas de P6 deben seguir en verde.

### P2 · Separar `OpticalFormulaController`

```java
// Boceto: mismas rutas, mismo contrato OpenAPI, cero cambios para el portal
@RestController
@RequestMapping("/api/patients/{patientId}/optical-formulas")
public class OpticalFormulaController {
    public OpticalFormulaController(RegisterOpticalFormulaUseCase register, ListOpticalFormulasUseCase list,
                                    UpdateOpticalFormulaUseCase update, DeleteOpticalFormulaUseCase delete) { ... }
}
```

El contrato REST no cambia, así que es una refactorización pura. Cada controlador queda con un solo
recurso. Si después de re-medir `PatientController` sigue con CBO > 10, el siguiente paso es mover el
mapeo DTO ↔ dominio a un `PatientWebMapper`.

### P3 · `useApiAction` y validación por reglas

```ts
// Boceto: elimina 8 copias del bloque try/catch/toast
export function useApiAction() {
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  async function run(action: () => Promise<unknown>, ok: string, fallback: string, after?: () => void) {
    setBusy(true);
    try { await action(); toast(ok); after?.(); }
    catch (e) { toast(e instanceof ApiError ? e.message : fallback, "err"); }
    finally { setBusy(false); }
  }
  return { busy, run };
}

// Boceto: FrameNew.validate pasa de 7 if con condiciones compuestas (V(G) 15) a una tabla de reglas
const reglasMontura: Regla<FrameForm>[] = [
  { campo: "sku", falla: (f) => !f.sku.trim(), mensaje: "Ingresa el SKU (BR-05)." },
  { campo: "sellPrice", falla: (f) => Number(f.sellPrice) < Number(f.buyPrice), mensaje: "BR-04 ..." },
  // ...
];
```

### P4 · `EyePrescription` como objeto de valor

```java
// Boceto: los 3 valores de cada ojo viajan juntos y BR-02 se valida en un solo lugar
public record EyePrescription(double sph, double cyl, int axis) {
    public EyePrescription {
        if (axis < 0 || axis > 180) throw new BusinessRuleException("Axis must be between 0 and 180"); // BR-02
    }
}
// OpticalFormula.register(patientId, EyePrescription od, EyePrescription oi, add, pd, current, date)
```

Solo cambia el dominio interno. La tabla (`pacientes.optical_formulas`, con columnas `od_sph`…) y el contrato JSON
plano se mantienen, y el mapeo se hace en `OpticalFormulaPersistenceAdapter`. Así el cambio no afecta a vc-db ni a
vc-portal.

### P6 · Red de pruebas antes de refactorizar

1. Agregar `jacoco-maven-plugin` a `vc-api/pom.xml` y publicar el reporte (`mvn verify`).
2. Pruebas unitarias para los 22 casos de uso sin prueba, empezando por `facturacion` (NFR-07 pide ≥ 90 %):
   `RegisterPaymentUseCase` (BR de sobrepago), `CreateInvoiceUseCase`, `WorkOrderApprovedListener`.
3. En el portal, pruebas con Testing Library de `PatientDetail`, `WorkOrderDetail` y `FrameNew` antes de P1
   y P3 (caracterizan el comportamiento actual).

---

## 4. Cómo se verificaría cada mejora (ciclo del módulo)

| Paso | Qué se hace | Evidencia |
|---|---|---|
| Medir | `mvn -q test -Dtest=MetricasMantenibilidadTest` y `python metricas/medir_frontend.py` | CSV "antes" en `metricas/resultados/` (ya generados) |
| Detectar | Comparar contra umbrales (`resumen.md`, `frontend-resumen.md`) | Este documento |
| Refactorizar | Aplicar P1…P5 en ramas `refactor/<propuesta>` con PR revisado por el otro integrante | Commits `refactor(...)` |
| Verificar | Volver a medir; las pruebas de P6 en verde; agregar tabla "valor antes / valor después" | Segunda tabla del Anexo C |
