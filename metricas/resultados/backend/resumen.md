# Resumen de metricas - vc-api

Archivos: 146 | Tipos: 154 | Metodos con cuerpo: 419

LOC fisicas 4367 | efectivas 3330 | comentario 149 (3.4%) | en blanco 888

## Distribucion de V(G) por metodo

| Riesgo | Metodos |
|---|---|
| Bajo | 419 |
| Moderado | 0 |
| Alto | 0 |
| Muy alto | 0 |

V(G) promedio 1.21 | maximo 4 | NLOC por metodo promedio 4.0 | maximo 26

## Top 10 metodos por V(G)

| Metodo | V(G) | NLOC | Param. | MI |
|---|---|---|---|---|
| Frame.register | 4 | 14 | 7 | 95.3 |
| PatientDetailResponse.from | 4 | 12 | 1 | 94.7 |
| Invoice.status | 4 | 10 | 0 | 104.1 |
| WorkOrder.advanceTo | 4 | 10 | 1 | 103.3 |
| WorkOrder.cancel | 4 | 9 | 1 | 105.7 |
| Document.Document | 4 | 9 | 0 | 108.1 |
| WorkOrderStatus.next | 4 | 8 | 0 | 111.7 |
| InvoicePersistenceAdapter.save | 3 | 13 | 1 | 93.4 |
| RegisterFrameUseCase.execute | 3 | 13 | 1 | 95.1 |
| JwtAuthenticationFilter.doFilterInternal | 3 | 13 | 3 | 95.2 |

## Metodos con mas de 5 parametros

- OpticalFormula.OpticalFormula: 12
- OpticalFormula.rehydrate: 12
- OpticalFormulaData.OpticalFormulaData: 12
- OpticalFormula.register: 11
- OpticalFormula.update: 10
- PatientController.PatientController: 9
- PatientData.PatientData: 9
- Invoice.Invoice: 8
- Invoice.rehydrate: 8
- Frame.Frame: 8
- Frame.rehydrate: 8
- FrameData.FrameData: 8
- WorkOrder.WorkOrder: 8
- WorkOrder.rehydrate: 8
- WorkOrderData.WorkOrderData: 8
- Patient.Patient: 8
- Patient.rehydrate: 8
- InvoiceData.InvoiceData: 7
- Frame.register: 7
- FrameController.FrameController: 6
- Frame.update: 6
- OrderStatusHistoryData.OrderStatusHistoryData: 6
- Patient.register: 6
- Patient.update: 6

## Suite CK - clases que superan algun umbral

| Clase | WMC | DIT | NOC | CBO | RFC | LCOM* | LCOM4 | Recomendacion |
|---|---|---|---|---|---|---|---|---|
| PatientController | 12 | 1 | 0 | 19 | 35 | 1.00 | 9 | Extract Class / Move Method (LCOM* 1.00, LCOM4 9 grupos); Facade o inyeccion de una abstraccion (CBO 19) |
| FrameController | 9 | 1 | 0 | 12 | 21 | 1.00 | 6 | Extract Class / Move Method (LCOM* 1.00, LCOM4 6 grupos); Facade o inyeccion de una abstraccion (CBO 12) |
| OrdenesConfig | 5 | 1 | 0 | 11 | 10 | 0.00 | 5 | Facade o inyeccion de una abstraccion (CBO 11) |
| WorkOrderController | 10 | 1 | 0 | 11 | 20 | 0.97 | 4 | Extract Class / Move Method (LCOM* 0.97, LCOM4 4 grupos); Facade o inyeccion de una abstraccion (CBO 11) |
| PacientesConfig | 9 | 1 | 0 | 11 | 18 | 0.00 | 9 | Facade o inyeccion de una abstraccion (CBO 11) |
| SecurityConfig | 4 | 1 | 0 | 11 | 38 | 1.00 | 1 | Facade o inyeccion de una abstraccion (CBO 11) |
| InvoiceController | 4 | 1 | 0 | 6 | 11 | 1.00 | 3 | Extract Class / Move Method (LCOM* 1.00, LCOM4 3 grupos) |
| Frame | 26 | 1 | 0 | 5 | 25 | 0.92 | 1 | Extract Class / Extract Method (WMC 26) |
| WorkOrder | 22 | 1 | 0 | 5 | 24 | 0.94 | 1 | Extract Class / Extract Method (WMC 22) |
| GetWorkOrderUseCase | 3 | 1 | 0 | 5 | 7 | 1.00 | 2 | Extract Class / Move Method (LCOM* 1.00, LCOM4 2 grupos) |
| PacientesFacadeImpl | 5 | 1 | 0 | 5 | 19 | 0.75 | 2 | Extract Class / Move Method (LCOM* 0.75, LCOM4 2 grupos) |
| Invoice | 21 | 1 | 0 | 4 | 32 | 0.94 | 1 | Extract Class / Extract Method (WMC 21) |
| InvalidPriceException | 1 | 6 | 0 | 2 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| OverpaymentException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| DuplicateSkuException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| InsufficientStockException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| CancelReasonRequiredException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| InvalidTransitionException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| OpticalFormula | 21 | 1 | 0 | 1 | 24 | 0.98 | 2 | Extract Class / Extract Method (WMC 21); Extract Class / Move Method (LCOM* 0.98, LCOM4 2 grupos) |
| DuplicateDocumentException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| NoCurrentFormulaException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |
| InvalidCredentialsException | 1 | 6 | 0 | 1 | 1 | 0.00 | 0 | Revisar la jerarquia: aplanarla o Replace Inheritance with Delegation (DIT 6) |

## Promedios por modulo

| Modulo | Tipos | WMC prom. | CBO prom. | LCOM prom. | MI prom. |
|---|---|---|---|---|---|
| (raiz) | 1 | 1.0 | 1.0 | 0.00 | 131.3 |
| facturacion | 22 | 4.0 | 2.7 | 0.20 | 124.9 |
| inventario | 29 | 3.9 | 3.0 | 0.12 | 124.3 |
| ordenes | 31 | 3.6 | 2.9 | 0.16 | 123.3 |
| pacientes | 39 | 4.4 | 2.9 | 0.14 | 121.1 |
| shared | 12 | 3.0 | 1.7 | 0.00 | 129.3 |
| usuarios | 20 | 2.6 | 3.2 | 0.18 | 121.1 |
