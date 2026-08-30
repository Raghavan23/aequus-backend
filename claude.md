# CLAUDE.md — Finz Backend

This file gives Claude (and any engineer) the context needed to work on this
codebase safely and consistently. Read this before making changes.

## What this is

Finz is a personal financial-record API. Multiple users register, log in, and
record their own `INCOME` / `EXPENSE` transactions against a **fixed** set of
categories. This is **V1** — deliberately simple. Do not add features not
listed in "In scope" below without being explicitly asked.

## Tech stack

- Java 21, Spring Boot 3.3.x
- Spring Security (stateless JWT auth, BCrypt password hashing)
- Spring Data JPA + PostgreSQL
- Flyway for migrations (never use `ddl-auto: update` — schema changes go
  through versioned migration files only)
- Maven

## Commands

```bash
mvn spring-boot:run          # run locally (reads application.yml env vars)
mvn test                     # run tests
mvn clean package            # build the jar
mvn dependency-check:check   # OWASP dependency vulnerability scan (add plugin if missing)
```

Local Postgres: `docker compose up -d` from the repo root (see
`docker-compose.yml`). Flyway auto-creates all tables on startup — never
create tables manually.

## Architecture — modular monolith

```
com.finz/
├── auth/        registration, login, JWT issuance
├── user/        user profile
├── financial/   financial records (the core domain)
└── common/      config, security, exception handling — shared by all modules
```

Rules for this structure:
- `auth`, `user`, and `financial` are independent domain modules. They should
  not directly depend on each other's internals — go through `common` or a
  public service method, not private fields.
- Each module keeps its own `controller/`, `service/`, `repository/`,
  `entity/`, `dto/`. Don't leak JPA entities across module boundaries or out
  through the REST API — always map to a DTO first.
- `common` has no knowledge of `auth`/`user`/`financial`. It must stay generic
  (see `AuthenticatedUser`, which deliberately avoids depending on the `User`
  JPA entity to prevent a `common → user` dependency).
- Do not turn this into microservices. Do not introduce Kafka, Redis, CQRS,
  or event sourcing unless a real V-next requirement demands it.

## In scope for this codebase

- Register / login / JWT auth
- CRUD on financial records, scoped to the authenticated user
- Fixed `FinancialType` (`INCOME`, `EXPENSE`) and `FinancialCategory` enums,
  validated so mismatched combinations (`INCOME` + `FOOD`) are rejected

## Explicitly out of scope — do not build unless asked

Analytics, dashboards, charts, reports, budgets, financial goals, AI
recommendations, recurring transactions, bank integrations, notifications,
CSV import/export, pagination, advanced search/filtering, admin/roles,
multi-tenancy, refresh tokens, OAuth/social login, MFA.

If a change would require one of these, stop and ask before implementing it.

## Security rules — non-negotiable

1. **Never trust a client-supplied user id.** Ownership is always derived
   from `CurrentUserProvider`, which reads the id out of the validated JWT.
   Request DTOs must never contain a `userId` field.
2. **Every financial-record query is scoped to `user_id`** at the repository
   layer (`findByIdAndUserId`, `findAllByUserIdOrderByCreatedAtDesc`, etc.).
   Never write a repository method that fetches a record by `id` alone if
   it's used from an authenticated endpoint.
3. **Passwords**: BCrypt only, via `PasswordEncoder`. Never log, return, or
   compare plaintext passwords. `UserResponse` must never expose
   `passwordHash`.
4. **Secrets** (`JWT_SECRET`, `DB_PASSWORD`) come from environment variables
   only. Never hardcode a real secret in `application.yml` or commit one to
   git. The default in `application.yml` is a dev-only placeholder — flag it
   loudly if you see it used outside local dev.
5. **Input validation**: every request DTO uses `jakarta.validation`
   annotations (`@NotNull`, `@Email`, `@DecimalMin`, etc.) and every
   controller method uses `@Valid`. Don't skip this for "obviously fine"
   fields.
6. **SQL injection**: use Spring Data JPA query methods or `@Query` with
   named/positional parameters. Never concatenate user input into a query
   string.
7. **CORS**: `finz.cors.allowed-origins` must be an explicit origin list in
   every environment, never `*`, especially once `allowCredentials(true)` is
   set (it already is).
8. **Error responses** must never leak stack traces, SQL, or internal class
   names to the client — `GlobalExceptionHandler` is the only place that
   builds error bodies. Extend it rather than throwing raw exceptions up to
   Spring's default handler.
9. When adding a dependency, check for known CVEs first (`mvn
   dependency-check:check` or check the advisory database) — don't just pull
   in the newest shiny library.

## Reliability & correctness

- Domain invariants (e.g. category-belongs-to-type) live in the domain layer
  (`FinancialCategory.validateBelongsTo`), not scattered across controllers.
- Use `BigDecimal` for all monetary values — never `float`/`double`. Money in
  Postgres is `NUMERIC(15,2)`.
- Timestamps are UTC (`@CreationTimestamp`/`@UpdateTimestamp`, `Instant`).
  Don't introduce local/zoned time without a clear reason.
- Wrap multi-step writes in `@Transactional`. Reads that don't mutate state
  use `@Transactional(readOnly = true)`.
- Return correct HTTP status codes: `201` on create, `204` on delete, `404`
  when a record isn't found *or* isn't owned by the caller (never `403` —
  don't reveal that a record exists but belongs to someone else).

## Maintainability & readability

- Keep controllers thin: parse request → call one service method → return.
  No business logic in controllers.
- Keep services focused on one aggregate (e.g. `FinancialRecordService` only
  touches financial records).
- Keep repositories persistence-only — no business rules inside a
  `Repository` interface.
- Prefer constructor injection (already used throughout) over field
  injection — it's easier to test and makes dependencies explicit.
- Name things after the domain, not the implementation
  (`FinancialRecordRequest`, not `CreateOrUpdateDto`).
- Favor small, single-purpose classes over generic/abstract base classes.
  Don't introduce a generic `CrudService<T>` or repository abstraction
  "for reuse" unless at least three concrete cases need it.
- Every new exception type extends the pattern in `common/exception` and is
  registered in `GlobalExceptionHandler`.

## Scalability

- Auth is stateless (JWT) — the app can run as multiple instances behind a
  load balancer with no session affinity.
- `user_id` is indexed on `financial_records` (see migrations) — keep new
  frequently-filtered columns indexed too.
- Don't add caching pre-emptively. If a real performance problem shows up,
  profile first, then decide between DB query optimization, indexing, or
  caching — in that order.
- When record volume grows, `GET /api/financial-records` will need
  pagination — flag this if record counts are expected to exceed a few
  hundred per user, but don't build it speculatively today.

## Testing expectations

- Any change to `FinancialCategory.validateBelongsTo` or the ownership checks
  in `FinancialRecordService` needs a test — these are the two places a bug
  becomes a security or data-integrity issue.
- Prefer testing services with mocked repositories over full
  `@SpringBootTest` where possible, for speed.
- New Flyway migrations should be additive (new `Vn__*.sql` file) — never
  edit a migration that has already shipped.

## Before you open a PR / finish a task

- [ ] Did I derive user ownership from the JWT, not from client input?
- [ ] Did I validate the request DTO?
- [ ] Did I add/adjust an index if I added a new frequently-queried column?
- [ ] Did I avoid exposing the JPA entity directly in a controller response?
- [ ] Did I check this doesn't reintroduce something from the "out of scope"
      list?
