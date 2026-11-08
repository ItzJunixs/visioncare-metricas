# vc-api

> VisionCare - Backend API: business logic and REST endpoints

Part of the **VisionCare** full-stack project — team `visioncare`, Complementaria III - Desarrollo Fullstack 2026-B.
Governance and documentation live in [`vc-docs`](https://github.com/code-corhuila/vc-docs).

## What it is

A Spring Boot 3 / Java 21 modular monolith: one deployable with five modules (`usuarios`,
`pacientes`, `inventario`, `ordenes`, `facturacion`). Each module exposes one public facade and
hides the rest in `internal`, in the layers `controller`, `dto`, `service`, `repository`,
`entity` and `config`. The design is in `vc-docs`: `05-architecture/`, `06-data/` and `07-api/`.

## Run and test

The database and the migrations come from `vc-db`; the whole stack starts from `vc-infra`
(`docker compose up`, see its README). To run the API alone against a database that is already up,
export the variables listed in `vc-infra/.env.example` (`SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`,
`APP_CORS_ALLOWED_ORIGINS`) and run:

```bash
mvn spring-boot:run
```

Tests need no database:

```bash
mvn test
```

| Test | What it checks |
|---|---|
| `RegisterFrameUseCaseTest`, `DeleteFrameUseCaseTest`, `ApproveWorkOrderUseCaseTest` | Business rules BR-04, BR-05, BR-07 and BR-09 in the services, with Mockito |
| `FrameControllerTest` | The web layer with `@WebMvcTest`: status codes, validation and the error shape |
| `ModuleBoundariesTest` | ArchUnit: no module reaches into another module's `internal` package |

## API documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`. OpenAPI JSON: `/v3/api-docs`.
The Postman collection in `postman/` logs in with the three roles and runs the checks for 401, 403,
the CRUDs and the business rule; import it and run it with the Collection Runner.

## Branching

Three permanent branches. **None of them accepts a direct commit** — you enter through a child
branch and leave through a Pull Request.

```
develop  <--PR--  feat/... fix/... chore/...
qa       <--PR--  develop
main     <--PR--  qa (release)   hotfix/...
```

`develop` is the default branch: start every task from it. `main` requires **1 approval from
`ariel5253`**. On `develop` and `qa` the team sets its own review rule.

Commit and branch naming: `00-governance/git-conventions.md` in `vc-docs`.
