# Requisitos de VisionCare v1.0.0

Fuente: repositorio de documentación de VisionCare (`vc-docs/04-requirements` y
`vc-docs/02-domain/entities-and-rules.md`). Este documento reúne los **10 requisitos funcionales** que
implementa el MVP y los **3 requisitos no funcionales** que se usan en el análisis de métricas, con
criterios de aceptación verificables y su trazabilidad hasta el código, las pruebas y las evidencias.

Roles del sistema: **ADMIN** (administrador), **SELLER** (vendedor/cajero) y **OPTOMETRIST** (optómetra).

## 1. Requisitos funcionales

| ID | HU | Requisito | Módulo | Criterios de aceptación |
|---|---|---|---|---|
| RF-01 | HU-01 | Registrar un paciente con tipo y número de documento, nombre, contacto y EPS | `pacientes` | **Dado** un documento que no existe, **cuando** se registra el paciente, **entonces** queda `ACTIVE` y aparece en la lista. Un tipo + número de documento repetido se rechaza con 409 (**BR-01**). |
| RF-02 | HU-02 | Registrar una fórmula óptica (SPH, CYL, EJE y ADD para OD y OI, distancia pupilar) | `pacientes` | Un eje fuera de 0–180 en cualquiera de los dos ojos se rechaza (**BR-02**). Guardar una fórmula como vigente reemplaza la vigente anterior del paciente (**BR-03**). |
| RF-03 | HU-03 | Buscar pacientes por nombre o documento | `pacientes` | La búsqueda devuelve las coincidencias. Si no hay ninguna, devuelve una lista vacía sin error. |
| RF-04 | HU-04 | Marcar el control periódico vencido | `pacientes` | Un paciente cuya fórmula vigente tiene más de 12 meses se muestra como `OVERDUE` en su detalle. |
| RF-05 | HU-05 | Registrar, editar y eliminar monturas del inventario | `inventario` | Un precio de venta menor que el de compra se rechaza (**BR-04**). Un SKU repetido se rechaza (**BR-05**). Solo ADMIN puede escribir; otro rol recibe 403. |
| RF-06 | HU-06 | Configurar el stock mínimo y alertar | `inventario` | Cuando el stock queda en el mínimo configurado o por debajo, la montura se marca como bajo el mínimo (`belowMinStock`) y se puede filtrar en el inventario. |
| RF-07 | HU-07 | Aprobar una orden de trabajo descontando stock | `ordenes` | Al aprobar, la montura descuenta 1 unidad y se registra el movimiento, todo en la misma transacción (**BR-07**, **BR-09**). Si el stock no alcanza, la aprobación se rechaza y nada cambia (**BR-06**). |
| RF-08 | HU-08 | Crear una orden de trabajo (cotización) | `ordenes` | La orden nace en `Cotización` (`QUOTE`) con código `OT-AAAA-NNNN` y una copia de la fórmula vigente. Si el paciente no tiene fórmula vigente, se rechaza. |
| RF-09 | HU-09 | Avanzar el estado de la orden de trabajo | `ordenes` | Solo se permite la secuencia `Cotización → Aprobada → En laboratorio → Lista → Entregada`, sin saltos (**BR-08**). Cancelar exige un motivo (**BR-10**). Cada cambio queda en el historial (**BR-11**). |
| RF-10 | HU-10 | Facturar la orden y registrar pagos | `facturacion` | Al aprobar la orden se emite la factura `FV-AAAA-NNNN` automáticamente. Un pago que supera el saldo se rechaza (**BR-13**). El estado se deriva del saldo: `PENDING`, `PARTIAL` o `PAID` (**BR-15**). |

Las HU-11 (portal del paciente) y HU-12 (tablero del personal con indicadores) están en el backlog de
VisionCare, pero **no forman parte del alcance de v1.0.0**.

## 2. Requisitos no funcionales

| ID | Categoría | Requisito | Criterio de medición | Estado en v1.0.0 |
|---|---|---|---|---|
| RNF-01 | Rendimiento | Las operaciones críticas responden rápido bajo carga normal (NFR-01 de VisionCare) | p95 < 300 ms con 50 usuarios concurrentes en `POST /api/work-orders/{id}/approve` y `GET /api/patients`, medido con prueba de carga antes de cada release | **No verificado**: no hay prueba de carga en el MVP |
| RNF-02 | Seguridad | Toda escritura exige autenticación y el rol correcto (NFR-05 de VisionCare) | 100 % de los endpoints de escritura responden 401 sin token y 403 con un rol no autorizado; se verifica con pruebas automatizadas | **Cumple**: `FrameControllerTest` y la colección de Bruno/Postman (37/37 peticiones, incluidos los casos 401 y 403) |
| RNF-03 | Mantenibilidad | El código se mantiene simple de probar y de modificar (propuesto por este proyecto) | Ningún método o función supera **V(G) = 10**; se verifica con `MetricasMantenibilidadTest` (backend) y `metricas/medir_frontend.py` (frontend) | **Cumple en backend** (máx. 4); **no cumple en frontend**: 7 funciones > 10, máx. 22 (ver `docs/metricas/02-resultados.md`) |

**Por qué estos tres.** Cubren tres atributos de calidad distintos (eficiencia de desempeño, seguridad y
mantenibilidad de ISO/IEC 25010). Además, RNF-03 es medible con las métricas del curso. VisionCare define
otro requisito de mantenibilidad, NFR-07 (cobertura ≥ 80 %), pero en v1.0.0 no se puede verificar porque
no hay herramienta de cobertura configurada. Por eso es la propuesta P6 del plan de mejoras.

## 3. Trazabilidad

| RF | Endpoint principal | Caso de uso (vc-api) | Pantalla (vc-portal) | Prueba automatizada | Evidencia (Anexo 1 de VisionCare) |
|---|---|---|---|---|---|
| RF-01 | `POST /api/patients` | `RegisterPatientUseCase` | `PatientNew` | Colección Bruno/Postman | 10, 11 |
| RF-02 | `POST /api/patients/{id}/optical-formulas` | `RegisterOpticalFormulaUseCase` | `PatientDetail` | Colección Bruno/Postman | 13, 14, 15 |
| RF-03 | `GET /api/patients?q=` | `SearchPatientsUseCase` | `Patients` | Colección Bruno/Postman | 11 |
| RF-04 | `GET /api/patients/{id}` | `GetPatientUseCase` | `PatientDetail` | — | 12 |
| RF-05 | `POST/PUT/DELETE /api/frames` | `RegisterFrameUseCase`, `UpdateFrameUseCase`, `DeleteFrameUseCase` | `Inventory`, `FrameNew` | `RegisterFrameUseCaseTest`, `DeleteFrameUseCaseTest`, `FrameControllerTest` | 05–09 |
| RF-06 | `PATCH /api/frames/{id}/min-stock` | `SetMinStockUseCase` | `Inventory` | Colección Bruno/Postman | 04 |
| RF-07 | `POST /api/work-orders/{id}/approve` | `ApproveWorkOrderUseCase` | `WorkOrderDetail` | `ApproveWorkOrderUseCaseTest` | 19 |
| RF-08 | `POST /api/work-orders` | `CreateWorkOrderUseCase` | `WorkOrderNew` | Colección Bruno/Postman | 16, 17, 18 |
| RF-09 | `PATCH /api/work-orders/{id}/status` | `AdvanceWorkOrderStatusUseCase` | `WorkOrderDetail` | Colección Bruno/Postman | 24 |
| RF-10 | `POST /api/invoices/{id}/payments` | `CreateInvoiceUseCase` (por evento), `RegisterPaymentUseCase` | `Invoices`, `InvoiceDetail` | Colección Bruno/Postman | 20–23 |

La columna *Prueba automatizada* evidencia el hallazgo de la sección 1.5 del análisis: solo RF-05 y RF-07
tienen pruebas unitarias. El resto se verifica únicamente con la colección de peticiones HTTP, que
requiere la aplicación levantada.
