# BloodBond

BloodBond is a real-time blood donor and recipient matching platform built with a Spring Boot backend, MySQL database, and a React + TypeScript frontend.

## Monorepo layout

- `backend/` — Spring Boot 3 / Java 21 / Maven application
- `frontend/` — Vite + React + TypeScript app
- `docs/` — API contract and planning docs
- `docker-compose.yml` — local MySQL database

## Local setup

### Run without Docker

The `local` Spring profile uses an in-memory H2 database, so Docker and MySQL are not required. Data is cleared when the backend process exits.

From `backend/`, run:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell, use `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`.

### Run with MySQL

1. Copy `.env.example` to `.env` and adjust values if needed.
2. Start the database:
   ```bash
   docker compose up -d mysql
   ```
3. Run the backend with the `dev` profile:
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
4. Run the frontend in another terminal:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Phase 1: Core setup (Auth & Profiles)

Phase 1 includes Flyway-backed user and profile tables, stateless JWT authentication, role-protected donor and recipient profile endpoints, and the landing, login, registration, and profile pages. Donor profiles require a date of birth confirming the donor is at least 18. Browser location is rounded to two decimal places before it is sent.

The `dev` Spring profile seeds an administrator if one does not already exist. Set `BLOODBOND_ADMIN_EMAIL` and `BLOODBOND_ADMIN_PASSWORD` in the backend process environment to change the local seed credentials; the sample values in `.env.example` are for local development only.

## Important notes

- Approximate coordinates only.
- No donor email or phone is returned in match payloads.
- AI is optional and defaulted to the mock provider.
- All database changes must be handled via Flyway migrations.
