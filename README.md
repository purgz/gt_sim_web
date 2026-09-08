# gt_sim_web
Web App for evolutionary simulator - see GTFyp


egt-platform/
├── backend/               ← Spring Boot
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── sim-service/           ← Python FastAPI  
│   ├── source/            ← your existing library
│   ├── main.py
│   ├── requirements.txt
│   └── Dockerfile
├── docker-compose.yml     ← local dev (all services)
├── docker-compose.prod.yml ← Oracle VM
└── .github/workflows/
    └── ci.yml

## Local development

1. Create a root `.env` (see variables referenced in `docker-compose.yml` and
   `backend/egt-api/src/main/resources/application.properties`: `DB_USER`, `DB_PASSWORD`,
   `JWT_SECRET`, `SIM_INTERNAL_TOKEN`, `MINIO_USER`, `MINIO_PASSWORD`).
2. Start the containers (Postgres, Redis, MinIO, sim-service):

   ```powershell
   docker compose up -d
   ```

3. Run the backend (port 8080) with the `dev` profile to seed dev users:

   ```powershell
   cd backend/egt-api
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   Seeded dev users (idempotent, created only if missing, passwords overridable via
   `DEV_ADMIN_PASSWORD` / `DEV_USER_PASSWORD`):

   | Email           | Password | Roles               |
   |-----------------|----------|---------------------|
   | admin@dev.local | admin123 | ROLE_ADMIN, ROLE_USER |
   | user@dev.local  | user123  | ROLE_USER           |

4. Run the frontend (port 4200):

   ```powershell
   cd frontend
   npm install
   npm start
   ```

## Tests

```powershell
cd backend/egt-api ; .\mvnw.cmd test      # backend (H2, no Docker needed)
cd frontend ; npm test                     # frontend (vitest)
cd sim_service ; pytest                    # sim service (needs pip deps installed)
```

To see the dev database
docker compose up -d
docker exec -it gt_sim_web-db-1 psql -U egt -d egt

![alt text](image.png)



redis:
docker exec -it gt_sim_web-redis-1 redis-cli
KEYS *
GET sim:<hash>
TTL sim:<hash>

minio:
http://localhost:9001