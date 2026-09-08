# gt_sim_web

Web app for an evolutionary game theory simulator — see [GTfyp](https://github.com/purgz/GTfyp).
See [docs/architecture.md](docs/architecture.md) for the arc42 architecture documentation.

```text
gt_sim_web/
├── backend/
│   └── egt-api/              ← Spring Boot REST API (Java 21)
│       ├── src/
│       ├── Dockerfile
│       └── pom.xml
├── frontend/                 ← Angular SPA
│   ├── src/
│   └── Dockerfile
├── sim_service/              ← Python FastAPI (wraps the GTfyp library)
│   ├── routers/
│   ├── models/
│   ├── main.py
│   ├── requirements.txt
│   └── Dockerfile
├── docker-compose.yml        ← local dev infra (Postgres, Redis, MinIO, sim-service)
├── docker-compose-prod.yml   ← deployment (Oracle VM, all services)
└── .github/
    └── workflows/
        └── ci.yml
```

A root `.env` (not committed) holds the secrets referenced by `docker-compose.yml` and
`backend/egt-api/src/main/resources/application.properties`: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`,
`SIM_INTERNAL_TOKEN`, `MINIO_USER`, `MINIO_PASSWORD`.

## Local development

1. Create a root `.env` with the variables listed above.
2. Start the containers (Postgres, Redis, MinIO, sim-service):

   ```powershell
   docker compose up -d
   ```

3. Run the backend (port 8080) with the `dev` profile to seed dev users:

   ```powershell
   cd backend/egt-api
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

   Seeded dev users (idempotent, created only if missing, passwords overridable via
   `DEV_ADMIN_PASSWORD` / `DEV_USER_PASSWORD`):

   | Email           | Password | Roles                 |
   |-----------------|----------|-----------------------|
   | admin@dev.local | admin123 | ROLE_ADMIN, ROLE_USER |
   | user@dev.local  | user123  | ROLE_USER             |

4. Run the frontend (port 4200):

   ```powershell
   cd frontend
   npm install
   npm start
   ```

## Tests

```powershell
cd backend/egt-api ; .\mvnw.cmd test      # backend (H2, no Docker needed)
cd frontend ; npm test                    # frontend (vitest)
cd sim_service ; pytest                   # sim service (needs pip deps installed)
```

## Debugging the local containers

Dev database (after `docker compose up -d`):

```powershell
docker exec -it gt_sim_web-db-1 psql -U egt -d egt
```

![Screenshot](image.png)

Redis:

```powershell
docker exec -it gt_sim_web-redis-1 redis-cli
KEYS *
GET sim:<hash>
TTL sim:<hash>
```

MinIO console: <http://localhost:9001>
