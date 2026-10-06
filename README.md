# Booking app

A simple booking app: a Spring Boot REST API backed by PostgreSQL, and a React frontend.

- List services (name, duration, price)
- Create a booking for a service, date and time slot, with customer name and email; the booking shows its total price
- View a booking by ID
- Cancel a booking

## Structure

```
backend/             Spring Boot 2.7 (Java 11, Gradle Wrapper) REST API
  src/main/java/com/example/booking/
    domain/          JPA entities (ServiceOffering, Booking)
    repository/      Spring Data repositories
    service/         BookingService (booking rules) and its exceptions
    web/             REST controllers, DTOs, error handling
  src/main/resources/db/migration/   Flyway migrations (schema + seed data)
frontend/            React 18 + Vite + TypeScript
  src/api.ts         API client
  src/components/    UI components and their tests
docker-compose.yml   PostgreSQL 15
.env.example         Database settings template
run-tests.sh         Runs all tests
```

## Prerequisites

- Java 11
- Node.js 18 or 20+ and npm
- Docker (only to run the app; tests don't need it)

Gradle does not need to be installed: always use the wrapper (`./gradlew`).

## Running the app

1. Create your environment file and start the database:

   ```bash
   cp .env.example .env        # then edit DB_PASSWORD
   docker compose up -d
   ```

2. Start the backend on http://localhost:8080. `bootRun` reads the repo-root `.env`, and Flyway creates the schema and seed data on startup:

   ```bash
   cd backend
   ./gradlew bootRun
   ```

   If you run the jar directly instead, export `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` yourself.

3. Start the frontend on http://localhost:5173 (it proxies `/api` to the backend):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Tests

Run everything from the repo root. It is non-interactive and exits non-zero if any test fails:

```bash
./run-tests.sh
```

Or run each side separately:

```bash
cd backend && ./gradlew test     # JUnit 5 unit + integration tests on in-memory H2 (no Docker)
cd frontend && npm test          # Vitest; JUnit XML written to frontend/test-results/junit.xml
```

Backend JUnit XML reports are in `backend/build/test-results/test/`.

## Build

```bash
cd backend && ./gradlew build    # jar in backend/build/libs/
cd frontend && npm run build     # type-check + bundle into frontend/dist/
```

## API

| Method | Path                        | Description      |
| ------ | --------------------------- | ---------------- |
| GET    | `/api/services`             | List services    |
| POST   | `/api/bookings`             | Create a booking |
| GET    | `/api/bookings/{id}`        | View a booking   |
| POST   | `/api/bookings/{id}/cancel` | Cancel a booking |

Create request:

```json
{
  "serviceId": 1,
  "date": "2030-01-15",
  "time": "10:00",
  "customerName": "Ada Lovelace",
  "customerEmail": "ada@example.com"
}
```

Errors are returned as `{ "status": 409, "message": "The 10:00 slot on 2030-01-15 is already booked" }`. Validation errors (400) also include a `fieldErrors` object keyed by field name.

## Booking rules

- Time slots are hourly, from 09:00 to 16:00 (last start time). Every service fits in one slot.
- A slot (date + time) can hold one confirmed booking, whichever service it is for. A second booking gets `409 Conflict`. Cancelling a booking frees its slot.
- A booking cannot start in the past (`400 Bad Request`), judged by the server's local time.
- The total price is the service price at the time of booking.

## Coding conventions

See [CLAUDE.md](CLAUDE.md).
