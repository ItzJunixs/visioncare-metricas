# VisionCare · Métricas de Mantenibilidad y Evolución

Proyecto final del curso **Métricas y Estándares de Calidad** (docente: José Miguel Llanos Mosquera).
Mide la calidad interna de **VisionCare v1.0.0** con cinco métricas de mantenibilidad y evolución,
interpreta los resultados frente a umbrales de referencia y **propone** mejoras justificadas. El código de
la aplicación no se modifica.

## 1. El proyecto medido

VisionCare es una aplicación web para ópticas independientes. Cubre todo el recorrido del paciente hasta
la entrega de sus gafas: registro de pacientes y fórmulas ópticas, inventario de monturas, órdenes de
trabajo con su secuencia de estados, y facturación con pagos parciales. La usan recepcionistas,
optómetras, vendedores/cajeros y administradores. Resuelve un problema concreto: antes, historias,
fórmulas, inventario, órdenes y pagos se llevaban en papel, hojas de cálculo y llamadas al laboratorio, con
precios y stock desincronizados entre copias.

## 2. Integrantes

| Integrante | GitHub | Aporte principal |
|---|---|---|
| Juan Diego Jiménez Horta | `@ItzJunixs` | Medidor estructural (V(G), LOC, Halstead, MI), medición del frontend, validación contra lizard, propuestas P1, P3 y P6 |
| Aura María Cabrera Ardila | `@<usuario>` | Suite CK y recomendador, costo de mantenimiento por tipo, gráficos, fichas, propuestas P2, P4, P5, P7 y P8 |

## 4. Arquitectura

**Monolito modular en capas** (ADR-001 de VisionCare). Hay un solo desplegable con cinco módulos de
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
solo hacia abajo entre capas y fronteras entre módulos verificadas automáticamente. 

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
    ├── informe-final.pdf       (pendiente)
    └── metricas/               fichas, resultados, análisis y plan de mejoras, gráficos
```

Repositorios originales: `github.com/code-corhuila/vc-api`, `vc-portal`, `vc-db`, `vc-infra`, `vc-docs`
(etiqueta `v1.0.0`).

## 10. Licencia y estado

Uso académico. Estado: **análisis completo, mejoras propuestas, sin implementar**.
Se usaron herramientas de IA como apoyo en la construcción de los medidores y la redacción. Su uso se
declara en el informe final, como exige la sección 8 de la guía.
