# VisionCare · Métricas de Mantenibilidad y Evolución

Proyecto final del curso **Métricas y Estándares de Calidad** (docente: José Miguel Llanos Mosquera).
Mide la calidad interna de **VisionCare v1.0.0** con cinco métricas de mantenibilidad y evolución,
interpreta los resultados frente a umbrales de referencia y **propone** mejoras justificadas. El código de
la aplicación no se modifica.

## 1. El proyecto medido

VisionCare es una aplicación web para ópticas independientes. Cubre todo el recorrido del paciente hasta
la entrega de sus gafas: registro de pacientes y fórmulas ópticas, inventario de monturas, órdenes de
trabajo con su secuencia de estados, y facturación con pagos parciales. La usan vendedores/cajeros,
optómetras y administradores. Resuelve un problema concreto: antes, historias,
fórmulas, inventario, órdenes y pagos se llevaban en papel, hojas de cálculo y llamadas al laboratorio, con
precios y stock desincronizados entre copias.

## 2. Integrantes

| Integrante | GitHub | Aporte principal |
|---|---|---|
| Juan Diego Jiménez Horta | `@ItzJunixs` | Medidor estructural (V(G), LOC, Halstead, MI), medición del frontend, validación contra lizard, propuestas P1, P3 y P6 |
| Aura María Cabrera Ardila | `@aura200128cabrera-sys` | Suite CK y recomendador, costo de mantenimiento por tipo, gráficos, fichas, propuestas P2, P4, P5, P7 y P8 |

## 3. Requisitos (resumen)

Criterios de aceptación y trazabilidad: [`docs/requisitos.md`](docs/requisitos.md).

| ID | Requisito | Módulo |
|---|---|---|
| RF-01 | Registrar un paciente | `pacientes` |
| RF-02 | Registrar una fórmula óptica (BR-02 eje 0–180, BR-03 una sola vigente) | `pacientes` |
| RF-03 | Buscar pacientes | `pacientes` |
| RF-04 | Marcar control periódico vencido | `pacientes` |
| RF-05 | Registrar una montura (BR-04 precio, BR-05 SKU único) | `inventario` |
| RF-06 | Configurar stock mínimo y alerta | `inventario` |
| RF-07 | Aprobar una orden de trabajo descontando stock (BR-09) | `ordenes` |
| RF-08 | Crear una orden de trabajo (cotización) | `ordenes` |
| RF-09 | Avanzar el estado de la orden (BR-08) | `ordenes` |
| RF-10 | Facturar la orden y registrar pagos | `facturacion` |
| RNF-01 | Rendimiento: p95 < 300 ms con 50 usuarios en aprobación y búsqueda | — |
| RNF-02 | Seguridad: 100 % de los endpoints de escritura rechazan peticiones sin autenticación o rol | — |
| RNF-03 | **Mantenibilidad: ningún método o función supera V(G) = 10** (verificado por `MetricasMantenibilidadTest`) | — |

## 4. Arquitectura

**Monolito modular en capas** (ADR-001 de VisionCare). Detalle y diagramas en [`docs/arquitectura/`](docs/arquitectura/README.md). Hay un solo desplegable con cinco módulos de
negocio que solo se comunican por su fachada pública. Las reglas ArchUnit de `ModuleBoundariesTest`
verifican esas fronteras.

```
vc-portal (React SPA) ──HTTP/JSON──> vc-api (Spring Boot, un desplegable)
                                       ├── pacientes   ─┐
                                       ├── inventario   │  cada módulo: Facade pública +
                                       ├── ordenes      │  internal/{controller, service,
                                       ├── facturacion  │  entity, repository, dto, config}
                                       ├── usuarios    ─┘
                                       └── shared (money, eventos, transacciones, errores)
                                            │
                                       PostgreSQL (un esquema por módulo)
```

Por qué favorece la mantenibilidad: un caso de uso por clase (métodos cortos, V(G) baja), dependencias
solo hacia abajo entre capas y fronteras entre módulos verificadas automáticamente. Las métricas
confirman este diseño en el backend (sección 7).

## 5. Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3.5.5, Spring Data JPA, Spring Security + JWT, ArchUnit, JUnit 5 |
| Frontend | React 18, TypeScript 5.6, Vite 5, Vitest, Testing Library |
| Base de datos | PostgreSQL 16 (migraciones Flyway en vc-db) |
| Medición | Medidor propio en Java (JUnit), lizard 1.25.1, Python 3.11+, matplotlib |

## 6. Instalación y ejecución

**Aplicación:** VisionCare se levanta con Docker Compose desde el repositorio `vc-infra`
(`docker compose up`). Las copias de `vc-api` y `vc-portal` de este repositorio son solo para medirlas.

**Requisitos para medir:** JDK 21, Maven 3.9+ y Python 3.11+.

```bash
# 1. Backend: medidor propio (CC, LOC, Halstead, MI, suite CK) + prueba del RNF-03
cd vc-api
mvn -q test -Dtest=MetricasMantenibilidadTest     # -> vc-api/target/metricas/*.csv y resumen.md
cd ..

# 2. Dependencias de los scripts
pip install -r metricas/requirements.txt

# 3. Frontend, validación cruzada, costo de mantenimiento y gráficos
python metricas/medir_frontend.py
python metricas/validar_backend_con_lizard.py
python metricas/costo_mantenimiento.py            # o: --repos ../vc-api ../vc-portal ../vc-db ../vc-infra ../vc-docs
python metricas/graficos.py                       # -> docs/metricas/graficos/*.png
```

Pruebas de la aplicación: `cd vc-api && mvn test` (backend) y `cd vc-portal && npm ci && npm test` (frontend).

## 7. Métricas aplicadas (resumen)

| Métrica | Cómo se mide | vc-api | vc-portal |
|---|---|---|---|
| V(G) McCabe | `ComplejidadCiclomatica.java` / lizard | máx. 4, 0 métodos > 10 | máx. **22**, **7** funciones > 10 |
| LOC | `MetricasLOC.java` / `comun.py` | 3 330 efectivas, 3,4 % comentarios, máx. 26 por método | 2 248 efectivas, **0,5 %** comentarios, **10** componentes > 50 |
| Índice de Mantenibilidad | `IndiceMantenibilidad.java` / `comun.py` | mín. 80,4 (moderada) | **10** funciones < 65 (mín. 37,6) |
| Suite CK | `SuiteCK.java` | CBO > 10 en 6 clases (máx. 19); WMC > 20 en 4; LCOM confirmado en 7 | no aplica (sin clases) |
| Costo por tipo | `costo_mantenimiento.py` | correctivo 8 %, adaptativo 26 %, perfectivo 58 %, preventivo 8 % (vs. 21/25/50/4) | — |

El medidor Java coincide con lizard en el **100 %** de los 291 métodos comparables y detecta 128 que
lizard omite. Fichas: [`docs/metricas/01-fichas-de-metricas.md`](docs/metricas/01-fichas-de-metricas.md) ·
Resultados: [`docs/metricas/02-resultados.md`](docs/metricas/02-resultados.md).

## 8. Mejoras propuestas

Detalle completo en [`docs/metricas/03-analisis-y-plan-de-mejoras.md`](docs/metricas/03-analisis-y-plan-de-mejoras.md).

| # | Propuesta | Técnica | Efecto esperado |
|---|---|---|---|
| P6 | Red de pruebas + JaCoCo (requisito previo) | Pruebas de caracterización | 3/25 → 25/25 casos de uso con prueba |
| P1 | Dividir `PatientDetail`, `WorkOrderDetail`, `WorkOrderNew` | Extract Component + Custom Hook | V(G) 22 → ≤ 10, MI 37,6 → > 65 |
| P3 | `useApiAction` + validación por tabla de reglas | Extract Function, Replace Conditional | V(G) 15 → ≤ 4; −8 bloques duplicados |
| P2 | Separar `OpticalFormulaController` | Extract Class | CBO 19 → ≈ 12 / ≈ 9 |
| P4 | `EyePrescription(sph, cyl, axis)` | Introduce Parameter Object | 12 → 8 parámetros |
| P5 | `Pricing` y `StockLevel` en `Frame` | Extract Class | WMC 26 → ≈ 16 |
| P7–P8 | Issues etiquetados con horas; métricas en CI | Proceso | Costo real medible; sin regresiones |

Las mejoras **no se implementaron** (alcance del proyecto: proponer). Tampoco se proponen cambios, por
estar justificados, para las clases `*Config` (CBO 11), la jerarquía de excepciones (DIT 6) ni las 14
clases con LCOM\* alto que LCOM4 muestra cohesivas.

## 9. Estructura del repositorio

```
visioncare-metricas/
├── README.md
├── vc-api/                     copia de VisionCare v1.0.0 (backend)
│   └── src/test/java/co/visioncare/vcapi/metricas/   <- medidor propio (CC, LOC, MI, CK) + JUnit
├── vc-portal/                  copia de VisionCare v1.0.0 (frontend)
├── metricas/                   scripts de medición, datos y resultados (CSV)
│   ├── datos/                  foto del historial de commits de los 5 repositorios
│   └── resultados/             salidas: backend/, frontend-*, costo, validación
└── docs/
    ├── informe-final.pdf       informe final del proyecto
    ├── requisitos.md           10 RF + 3 RNF con criterios de aceptación y trazabilidad
    ├── arquitectura/           estilo, ADR, diagramas (Mermaid + PNG) y modelo de datos
    └── metricas/               fichas, resultados, análisis y plan de mejoras, gráficos
```

Repositorios originales: `github.com/code-corhuila/vc-api`, `vc-portal`, `vc-db`, `vc-infra`, `vc-docs`
(etiqueta `v1.0.0`).

## 10. Licencia y estado

Uso académico. Estado: **entregado (etiqueta `v1.0.0`)**: análisis completo, mejoras propuestas, sin implementar. Informe final: [`docs/informe-final.pdf`](docs/informe-final.pdf).
Se usaron herramientas de IA como apoyo en la construcción de los medidores y la redacción. Su uso se
declara en el informe final, como exige la sección 8 de la guía.
