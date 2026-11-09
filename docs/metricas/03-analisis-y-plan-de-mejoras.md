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

### P6 · Red de pruebas antes de refactorizar

1. Agregar `jacoco-maven-plugin` a `vc-api/pom.xml` y publicar el reporte (`mvn verify`).
2. Pruebas unitarias para los 22 casos de uso sin prueba, empezando por `facturacion` (NFR-07 pide ≥ 90 %):
   `RegisterPaymentUseCase` (BR de sobrepago), `CreateInvoiceUseCase`, `WorkOrderApprovedListener`.
3. En el portal, pruebas con Testing Library de `PatientDetail`, `WorkOrderDetail` y `FrameNew` antes de P1
   y P3 (caracterizan el comportamiento actual).

---

