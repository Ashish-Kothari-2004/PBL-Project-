# BloodBond API reference

Base URL for local development: `http://localhost:8080/api`

Unless noted as public, endpoints require a JWT in the `Authorization` header:

```http
Authorization: Bearer <token>
```

Successful responses use the `ApiResponse<T>` envelope:

```json
{
  "timestamp": "2026-10-22T00:00:00Z",
  "status": 200,
  "message": "Operation completed",
  "data": {}
}
```

## Authorization overview

| Endpoint | Public | DONOR | RECIPIENT | ADMIN |
|---|:---:|:---:|:---:|:---:|
| `GET /health` | Yes | Yes | Yes | Yes |
| `POST /auth/register`, `POST /auth/login` | Yes | Yes | Yes | Yes |
| `GET /users/me` | No | Yes | Yes | Yes |
| `/donors/me` | No | Yes | No | No |
| `/recipients/me` | No | No | Yes | No |
| `GET /match/search` | No | No | Yes | No |
| `GET /chat/rooms` and room messages | No | Participant only | Participant only | No rooms unless participant |
| `POST /chat/rooms` | No | No | Yes | No |
| `/assistant/messages` | No | Yes | Yes | No |
| `POST /requests`, `GET /requests/mine` | No | No | Yes | No |
| `DELETE /requests/{id}` | No | No | Yes | No |
| `GET /requests/open`, `POST /requests/{id}/respond` | No | Yes | No | No |
| `GET /admin/overview` | No | No | No | Yes |

The local H2 web console is at `http://localhost:8080/h2-console` when running with the `local` Spring profile. It is a development tool, not an application API.

## Health

### `GET /api/health`

Public backend health check.

```json
{
  "timestamp": "2026-10-22T00:00:00Z",
  "status": 200,
  "message": "BloodBond service is running",
  "data": { "status": "UP" }
}
```

## Authentication

### `POST /api/auth/register`

Public. Creates a DONOR or RECIPIENT account and returns a JWT. ADMIN cannot be self-registered.

Request:

```json
{
  "name": "Sam Lee",
  "email": "sam@example.com",
  "password": "ExamplePass123!",
  "role": "RECIPIENT"
}
```

Password must be 8–72 characters and contain uppercase, lowercase, a number, and a special character.

### `POST /api/auth/login`

Public.

```json
{
  "email": "sam@example.com",
  "password": "ExamplePass123!"
}
```

Registration and login return:

```json
{
  "timestamp": "2026-10-22T00:00:00Z",
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "<jwt>",
    "user": {
      "id": 12,
      "name": "Sam Lee",
      "email": "sam@example.com",
      "role": "RECIPIENT"
    }
  }
}
```

Register responds with HTTP `201`; login responds with HTTP `200`.

### `GET /api/users/me`

Authenticated. Returns the current account summary (`id`, `name`, `email`, `role`).

## Profiles

### Donor profile

Both routes require the DONOR role.

- `GET /api/donors/me` loads the current donor profile.
- `PUT /api/donors/me` creates or updates it.

Example request:

```json
{
  "bloodGroup": "O+",
  "dateOfBirth": "1990-03-12",
  "city": "Springfield",
  "approxLat": 37.42,
  "approxLng": -122.08,
  "isAvailable": true
}
```

Supported blood groups: `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`. Date of birth is required for updates and the donor must be at least 18. Coordinates, when supplied, must be within latitude `-90..90` and longitude `-180..180`.

Profile response data:

```json
{
  "id": 5,
  "userId": 12,
  "bloodGroup": "O+",
  "dateOfBirth": "1990-03-12",
  "city": "Springfield",
  "approxLat": 37.42,
  "approxLng": -122.08,
  "isAvailable": true,
  "verificationStatus": "PENDING"
}
```

### Recipient profile

Both routes require the RECIPIENT role.

- `GET /api/recipients/me` loads the current recipient profile.
- `PUT /api/recipients/me` creates or updates it.

Example request:

```json
{
  "city": "Springfield",
  "approxLat": 37.42,
  "approxLng": -122.08
}
```

Response data contains `id`, `userId`, `city`, `approxLat`, and `approxLng`. Coordinate ranges follow the same latitude/longitude bounds as donor profiles.

## Donor discovery and map

### `GET /api/match/search`

RECIPIENT role only. Returns available donor profiles with saved coordinates that are within range and not marked as ineligible by a future `nextEligibleDate`. Results are ordered nearest first.

Query parameters:

| Name | Required | Default | Meaning |
|---|---:|---:|---|
| `lat` | Yes | — | Recipient latitude (`-90..90`) |
| `lng` | Yes | — | Recipient longitude (`-180..180`) |
| `bloodGroup` | No | No group filter | One of the supported blood groups |
| `radiusKm` | No | `50` | Search radius, from `1` to `250` km |

Example:

```http
GET /api/match/search?lat=37.42&lng=-122.08&bloodGroup=O%2B&radiusKm=50
```

Response `data` is an array:

```json
[
  {
    "donorProfileId": 5,
    "donorName": "Alex",
    "bloodGroup": "O+",
    "city": "Springfield",
    "approxLat": 37.42,
    "approxLng": -122.08,
    "distanceKm": 0.0
  }
]
```

The web client captures browser location only when a user chooses the approximate-location action, rounds coordinates to two decimals, and sends them to the profile endpoint. The frontend uses Leaflet to render the recipient’s approximate location and returned donor pins on OpenStreetMap tiles. Selecting a donor opens a private chat. Tiles are loaded by the browser and require internet access.

Match results intentionally omit donor email, phone number, date of birth, and exact coordinates. Coordinates and blood-group data are not a clinical compatibility or eligibility decision.

## Blood requests

Requests let a recipient notify donors without exposing private contact details. They are separate from direct donor discovery and do not establish clinical compatibility.

### `POST /api/requests`

RECIPIENT role only. Creates an open request with a supported blood group, optional approximate city/coordinates, and a search radius (`1..250` km). Supply both coordinates or neither. The donor request feed filters by the stored approximate coordinates and radius when both profiles have coordinates; otherwise it uses an exact case-insensitive city match. The recipient UI requires explicit opt-in before sharing saved approximate coordinates.

```json
{
  "bloodGroup": "O+",
  "city": "Springfield",
  "approxLat": 37.42,
  "approxLng": -122.08,
  "radiusKm": 50
}
```

### `GET /api/requests/mine`

RECIPIENT role only. Returns the current user's requests and response statuses. Donor identities are not exposed in the response list.

### `DELETE /api/requests/{requestId}`

RECIPIENT role only. Cancels the current user's open request. Fulfilled, already-cancelled, or other users' requests return `404`.

### `GET /api/requests/open`

DONOR role only. Lists open, unanswered requests matching the donor's approximate radius or city, without recipient identity or contact information.

### `POST /api/requests/{requestId}/respond`

DONOR role only. Donor profile must exist and be marked available. A donor may respond once:

```json
{ "decision": "ACCEPTED" }
```

`DECLINED` records the response and leaves the request open. `ACCEPTED` marks it fulfilled and creates a private chat room; the recipient receives a WebSocket room notification. An accepted chat is an invitation to communicate, not clinical clearance.

## BloodBond assistant

All assistant routes require an authenticated DONOR or RECIPIENT account.

- `GET /api/assistant/messages` returns the latest 100 messages for the current account.
- `POST /api/assistant/messages` sends a message and returns both the persisted user message and assistant reply. Message text is required and limited to 2,000 characters.
- `DELETE /api/assistant/messages` deletes the current account's assistant conversation and stored messages.

The backend retains the assistant conversation in its database and sends at most the latest 12 messages plus the account role to Groq for a response. It does not include user-to-user private chat history, profile data, account name, email, location, or credentials. Users should still avoid sharing identifying or sensitive medical information. The API key is read server-side from `GROQ_API_KEY` in the process environment or a root/backend `.env` file (the key name is case-insensitive and quoted values are accepted); Groq endpoint/model are configurable with `BLOODBOND_GROQ_URL` and `BLOODBOND_GROQ_MODEL`. The configured default model is `qwen/qwen3.8-27b`. Missing keys and provider failures return HTTP `503` rather than a mock answer.

The assistant is educational, not a clinical decision system. It must not determine individual donor-recipient compatibility, diagnose, prescribe, or clear a person to donate. Blood-bank professionals and treating clinicians make those decisions.

## Admin overview

### `GET /api/admin/overview`

ADMIN role only. Returns aggregate totals for users, donor/recipient accounts, open/fulfilled requests, chat rooms, and donor profiles with pending review. It contains no user-level personal information. A pending review count is not verification or clinical clearance.

## Private chat

Chat updates are delivered over authenticated STOMP messaging using a SockJS WebSocket endpoint. REST is still used to load initial conversation and message history.

### WebSocket connection

Connect to `http://localhost:8080/ws` using SockJS and include the JWT as a STOMP `CONNECT` native header:

```text
Authorization: Bearer <token>
```

The backend validates the JWT during STOMP connection setup. Clients subscribe to:

| Destination | Purpose | Access |
|---|---|---|
| `/topic/chat/rooms/{roomId}` | New messages in a conversation | Donor or recipient participant only |
| `/user/queue/chat/rooms` | New room notifications | Authenticated user; server routes it to that user |

To send a message, publish JSON to `/app/chat/rooms/{roomId}/messages`:

```json
{ "content": "Hello, I saw your donor profile." }
```

The server validates the connection user and room participation, persists the message, and broadcasts the created `ChatMessageResponse` to `/topic/chat/rooms/{roomId}`. Messages are delivered to all authorized subscribers, including the sender. Content must be non-empty and at most 2,000 characters. Clients cannot publish directly to `/topic` or `/user` destinations.

When a recipient creates a new chat room through `POST /api/chat/rooms`, the backend also notifies the donor on `/user/queue/chat/rooms`. The frontend updates its conversation list from this event without polling.

### `GET /api/chat/rooms`

Authenticated. Returns only rooms where the current user is a donor or recipient participant. Each room contains:

```json
{
  "id": 9,
  "bloodRequestId": 16,
  "counterpartName": "Alex",
  "bloodGroup": "O+",
  "city": "Springfield",
  "createdAt": "2026-10-22T00:00:00"
}
```

### `POST /api/chat/rooms`

RECIPIENT role only. Starts a conversation with a donor profile. A blood request and chat room are created for a new donor-recipient pair. If a room for that pair already exists, it is returned rather than creating another. A new room responds with HTTP `201`; an existing room responds with HTTP `200`.

Request:

```json
{
  "donorProfileId": 5,
  "bloodGroup": "O+",
  "city": "Springfield",
  "approxLat": 37.42,
  "approxLng": -122.08
}
```

`donorProfileId` and a valid blood group are required. A donor marked unavailable cannot be contacted through this route.

### `GET /api/chat/rooms/{roomId}/messages`

Donor or recipient participant only. Returns the room’s messages in ascending creation order:

```json
[
  {
    "id": 31,
    "senderId": 12,
    "senderName": "Sam Lee",
    "content": "Hello, I saw your donor profile.",
    "createdAt": "2026-10-22T00:00:00"
  }
]
```

### `POST /api/chat/rooms/{roomId}/messages`

Donor or recipient participant only. `content` must be non-empty and no longer than 2,000 characters.

```json
{ "content": "Hello, I saw your donor profile." }
```

The response `data` contains the created message object. Other authenticated users cannot read or send messages in a room; unknown and non-participant room IDs return HTTP `404`.

Chat is for coordination only. Avoid sharing identity documents, precise addresses, or sensitive medical information in messages.

## Storage and migrations

Flyway migrations are in `backend/src/main/resources/db/migration/`.

- `V1__init.sql`: `users`, `donor_profiles`, and `recipient_profiles`.
- `V2__matching_and_chat.sql`: `blood_requests`, `chat_rooms`, and `chat_messages`, with participant/request foreign keys and lookup indexes.
- `V3__assistant_and_request_workflow.sql`: account-scoped assistant conversations/messages and unique donor responses to blood requests.

Local profile: H2 in-memory database, Flyway enabled, H2 console enabled. MySQL is the default configuration outside the local profile.

## Errors

Validation and application errors use this response shape:

```json
{
  "timestamp": "2026-10-22T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Radius must be between 1 and 250 km",
  "path": "/api/match/search"
}
```

Common statuses:

- `400 Bad Request`: invalid request body, coordinates, blood group, or search radius.
- `401 Unauthorized`: missing/invalid JWT or invalid login.
- `403 Forbidden`: authenticated role is not allowed to call a role-protected endpoint.
- `404 Not Found`: profile/resource is missing or chat room is not accessible to the current user.
- `503 Service Unavailable`: the Groq assistant is not configured or the provider did not return a usable answer.

Optional automated request analysis is not part of the current feature set. The assistant can explain general donation and BloodBond workflows but does not analyze an individual's clinical request.
