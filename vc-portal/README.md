# vc-portal

> VisionCare - Web portal: React front-end for the optical-store staff

Part of the **VisionCare** full-stack project — team `visioncare`, Complementaria III - Desarrollo Fullstack 2026-B.
Governance and documentation live in [`vc-docs`](https://github.com/code-corhuila/vc-docs).

## What it is

A React 18 + TypeScript single-page application (Vite). Staff sign in with a username and password;
the portal stores the JWT and sends it in the `Authorization` header of every request
(`src/api.ts`). Routes are protected by a guard (`RequireAuth`) and by role (`RequireRole` in
`src/App.tsx`), and every error returned by the API is shown to the user in a toast or banner.

| Screen | Roles | CRUD |
|---|---|---|
| Patients (list, detail, create, edit, deactivate) | ADMIN, SELLER, OPTOMETRIST | Patient |
| Optical formulas (inside the patient detail: list, register, edit, delete) | ADMIN, OPTOMETRIST | OpticalFormula |
| Inventory (list, create, edit, delete, minimum stock) | ADMIN writes; SELLER reads | Frame |
| Work orders and invoices | ADMIN, SELLER | Business rule BR-09 |

## Run and test

The whole stack starts from `vc-infra` (`docker compose up`). To work on the portal alone, with the
API on `localhost:8080`:

```bash
npm install
npm run dev      # http://localhost:5173, /api is proxied to the API
npm test         # Vitest + Testing Library
npm run build
```

Set `VITE_API_PROXY` to point the dev proxy at another API address.

## Tests

| File | What it checks |
|---|---|
| `src/__tests__/api.test.ts` | The client sends the Bearer token, raises `ApiError` with the API message, clears the session on 401, and builds readable messages for 400 and 403 |
| `src/__tests__/RequireRole.test.tsx` | The route guard redirects to `/login` without a session and lets a signed-in user through |

## Docker

`Dockerfile` builds the SPA and serves it with nginx; `nginx.conf` forwards `/api` to the `api`
container, so the browser only talks to one origin.

## Branching

`develop` <- `feat/...` | `fix/...` | `chore/...` (Pull Request); `qa` <- `develop`; `main` <- `qa`
with the instructor's approval. Naming rules: `00-governance/git-conventions.md` in `vc-docs`.
