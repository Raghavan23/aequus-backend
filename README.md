# Aequus — Personal Financial Operating System

A modern personal financial operating system. Users register, log in, manage transactions, track categories, and leverage financial intelligence.

```
aequus/
├── backend/     Spring Boot 3 (Java 21) REST API (Modular Monolith)
├── frontend/    Angular 19 SPA
└── docker-compose.yml   Local Postgres for development
```

---

## 1. Prerequisites

* Java 21 (JDK)
* Maven 3.9+ (or use your IDE's bundled Maven)
* Node.js 20+ and npm 10+
* Angular CLI 19 (`npm install -g @angular/cli`)
* PostgreSQL 16 (or Docker, to run the provided `docker-compose.yml`)

---

## 2. Database setup

### Option A — Docker (recommended)

```bash
docker compose up -d
```

This starts Postgres on `localhost:5432` with:
* database: `aequus`
* user: `aequus`
* password: `aequus`

### Option B — Existing Postgres instance

Create a database and user yourself:

```sql
CREATE DATABASE aequus;
CREATE USER aequus WITH ENCRYPTED PASSWORD 'aequus';
GRANT ALL PRIVILEGES ON DATABASE aequus TO aequus;
```

No manual table creation is needed — Flyway runs the migrations in
`backend/src/main/resources/db/migration` automatically on startup.

---

## 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### Environment configuration

All configuration is externalized via environment variables (see
`backend/src/main/resources/application.yml`). Defaults work out of the box with
the Docker Postgres above; override as needed:

| Variable               | Default                                             | Purpose                          |
|-------------------------|-----------------------------------------------------|-----------------------------------|
| `DB_URL`                | `jdbc:postgresql://localhost:5432/aequus`           | JDBC connection string            |
| `DB_USERNAME`           | `aequus`                                            | Database user                     |
| `DB_PASSWORD`           | `aequus`                                            | Database password                 |
| `SERVER_PORT`           | `8080`                                              | API port                          |
| `JWT_SECRET`            | *(dev default, change in production)*               | HMAC signing key for JWTs         |
| `JWT_EXPIRATION_MS`     | `86400000` (24h)                                    | Token lifetime                    |
| `CORS_ALLOWED_ORIGINS`  | `http://localhost:4200`                             | Comma-separated allowed origins   |

**Important:** set a strong, random `JWT_SECRET` (32+ bytes) before deploying anywhere
beyond local development.

---

## 4. Run the frontend

```bash
cd frontend
npm install
npm start
```

The app runs on `http://localhost:4200` and talks to the API at
`http://localhost:8080/api` (see `src/environments/environment.ts`).

For a production build:

```bash
npm run build
```

Output is written to `dist/aequus-frontend`. Update
`src/environments/environment.production.ts` if the API is hosted at a different
path than `/api`.
