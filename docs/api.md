# BloodBond API contract

This file captures the initial contract for the MVP and is updated as endpoints change.

## Health

### `GET /api/health`

Returns the status of the backend service.

Example response:

```json
{
  "timestamp": "2026-10-09T12:00:00Z",
  "status": 200,
  "message": "BloodBond service is running",
  "data": {
    "status": "UP"
  }
}
```

## Phase 1 endpoints

- `POST /api/auth/register` — public; accepts a DONOR or RECIPIENT role and a strong password.
- `POST /api/auth/login` — public; returns a JWT and user summary.
- `GET /api/users/me` — requires a bearer token.
- `GET /api/donors/me`, `PUT /api/donors/me` — DONOR only; profile updates require a date of birth showing the donor is at least 18.
- `GET /api/recipients/me`, `PUT /api/recipients/me` — RECIPIENT only.
- `/api/admin/**` — ADMIN only; an admin account is seeded only with the `dev` Spring profile.

Profile coordinates are approximate; the frontend rounds browser coordinates to two decimal places before sending.

## Planned Phase 2+ endpoints

- `GET /api/match/search`
- `POST /api/requests`
- `GET /api/requests/mine`
- `POST /api/requests/{id}/respond`
- `GET /api/chat/rooms`
- `GET /api/chat/rooms/{id}/messages`
- `POST /api/ai/analyze-request`
- `GET /api/admin/stats`

## Error format

All errors follow this structure:

```json
{
  "timestamp": "2026-10-09T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register"
}
```
