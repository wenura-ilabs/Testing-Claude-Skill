# CLAUDE.md

Simple booking app. Keep it simple: don't add features, layers or dependencies that weren't asked for.

## Structure

- `backend/` — Spring Boot 2.7.x, Java 11, Gradle via the wrapper. Package `com.example.booking`:
  - `domain/` JPA entities, `repository/` Spring Data repositories, `service/` business rules and exceptions, `web/` controllers, `web/dto/` request/response classes, `config/` beans.
  - `src/main/resources/db/migration/` Flyway migrations.
  - `src/test/` JUnit 5 tests; `src/test/resources/application-test.properties` is the H2 `test` profile.
- `frontend/` — React 18 + Vite 5 + TypeScript. `src/api.ts` is the only place that calls `fetch`; components live in `src/components/` with tests next to them (`*.test.tsx`).
- `docker-compose.yml` — PostgreSQL 15. `.env.example` — database settings template. `run-tests.sh` — all tests.

## Commands

Gradle is not installed on this machine. Always use `./gradlew` from `backend/`, never `gradle`.

```bash
cp .env.example .env && docker compose up -d   # start the database (needs .env)
./run-tests.sh                                 # all tests, from the repo root; non-zero exit on failure

cd backend
./gradlew test                                 # backend tests (H2, no Docker needed)
./gradlew test --tests '*BookingServiceTest'   # a single test class
./gradlew bootRun                              # run the API on :8080 (reads ../.env)
./gradlew build

cd frontend
npm test                                       # Vitest, single run; JUnit XML in test-results/junit.xml
npm run dev                                    # dev server on :5173, proxies /api to :8080
npm run build                                  # type-check + bundle
```

## Version constraints

- Spring Initializr no longer offers Boot 2.7, so `backend/build.gradle` pins Boot 2.7.18 and the wrapper pins Gradle 7.6.4 by hand. Don't upgrade either: Boot 3+ and Gradle 9+ need Java 17.
- The frontend stays on Vite 5 / Vitest 2 so it runs on the installed Node 21.

## Conventions

Backend
- Java 11 and Spring Boot 2.7: `javax.*` imports (not `jakarta.*`), no records, no Lombok.
- Tabs for indentation; constructor injection, no field injection.
- Controllers stay thin: validate the request (`@Valid` + Bean Validation messages on the DTO), call the service, map to a response DTO. Never return entities from controllers.
- Business rules live in `BookingService` and fail by throwing `NotFoundException` (404), `InvalidBookingException` (400) or `ConflictException` (409). `ApiExceptionHandler` turns these into `ErrorResponse` JSON; error messages must be clear enough to show to a user as-is.
- Use the injected `Clock` for the current time, never a bare `LocalDateTime.now()`.
- Schema changes go in a new Flyway migration (`V3__...sql`); never edit an applied migration. SQL must run on both PostgreSQL and H2 (PostgreSQL mode). Hibernate runs with `ddl-auto=validate`.
- Double-booking is prevented by a check in the service and by the `uq_bookings_active_slot` unique constraint (`active_slot` is TRUE for confirmed bookings and NULL for cancelled ones). Keep both.
- Promo code rules live in `PromoCodeService`; discount maths lives in `PromoCode.discountFor`. `BookingService.create` runs the promo check last, after every other booking check, so a rejected booking never uses up a code. Promo rejections are `InvalidBookingException` (400) with the exact messages listed in the README.
- A code's usage limit is enforced by locking its `promo_codes` row (`findByCodeForUpdate`) before counting confirmed bookings, inside the booking transaction. Don't replace this with a plain count: `usageLimitHoldsUnderConcurrentBookings` fails without the lock.
- `BookingService` keeps its 3-argument constructor and 5-argument `create` for bookings without a promo code.
- Don't log customer names or emails.
- Database credentials come only from environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`). Never hardcode them or commit `.env`.
- Tests: unit tests use Mockito and a fixed `Clock`; integration tests use `@SpringBootTest` + `MockMvc` with `@ActiveProfiles("test")`.

Frontend
- Function components with hooks, TypeScript strict mode, no `any`.
- 2-space indentation, single quotes, no semicolons.
- Show the server's error `message` (and `fieldErrors`) to the user; errors render with `role="alert"`.
- Tests use Vitest + Testing Library; query by role or label, and mock `src/api.ts` rather than `fetch` in component tests.
- The time slots in `src/format.ts` mirror the slot hours in `BookingService`; change them together.

## Verifying changes

Run `./run-tests.sh` before finishing. For frontend changes also run `npm run build` to type-check.
