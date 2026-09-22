# Aequus (AutoLedger) — Autonomous Financial Close & Reconciliation Engine

> *"We're building the autonomous CFO — an AI agent that closes the books in hours instead of weeks, starting with India's 70,000 CA firms and expanding to global mid-market enterprises."*

A modern, high-throughput autonomous accounting reconciliation engine. It connects raw bank feeds, vision-parsed invoice PDFs, and ledger exports to automatically reconcile transactions, detect duplicates and anomalies, and provide an immutable audit trail for CA firms and finance departments.

```
aequus/
├── backend/     Spring Boot 3 (Java 21) REST API (Modular Monolith)
├── frontend/    Angular 19 SPA (Reconciliation UI, Client Hub, Audit Trail)
└── docker-compose.yml   Local Postgres for development
```

---

## 🚀 Key Features

- **Multi-Tenant CA Portfolio Management**: Seamlessly manage dozens of client books with strict data isolation.
- **Smart Bank Statement Ingestion**: High-precision parsers for CSV and PDF bank feeds with duplicate detection.
- **Multimodal Invoice & Receipt OCR**: Extract line items, GSTIN numbers, and tax breakdowns via Vision LLMs.
- **Hybrid Matching Engine**: Deterministic Spring Boot rule engine combined with LLM fuzzy reasoning and confidence scoring.
- **Anomaly Detection & Review Workspace**: Fast human-in-the-loop review for unmatched entries and tax discrepancies.
- **Immutable Audit Trail**: Append-only compliance logging with SHA-256 integrity proofs.

---

## 1. Prerequisites

* Java 21 (JDK)
* Maven 3.9+ (or use `./mvnw`)
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

Flyway runs migrations in `backend/src/main/resources/db/migration` automatically on startup.

---

## 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### Environment configuration

| Variable               | Default                                             | Purpose                          |
|-------------------------|-----------------------------------------------------|-----------------------------------|
| `DB_URL`                | `jdbc:postgresql://localhost:5432/aequus`           | JDBC connection string            |
| `DB_USERNAME`           | `aequus`                                            | Database user                     |
| `DB_PASSWORD`           | `aequus`                                            | Database password                 |
| `SERVER_PORT`           | `8080`                                              | API port                          |
| `JWT_SECRET`            | *(dev default, change in production)*               | HMAC signing key for JWTs         |
| `JWT_EXPIRATION_MS`     | `86400000` (24h)                                    | Token lifetime                    |
| `CORS_ALLOWED_ORIGINS`  | `http://localhost:4200`                             | Comma-separated allowed origins   |

---

## 4. Run the frontend

```bash
cd frontend
npm install
npm start
```

The app runs on `http://localhost:4200` and talks to the API at `http://localhost:8080/api`.

For a production bundle:

```bash
npm run build
```
