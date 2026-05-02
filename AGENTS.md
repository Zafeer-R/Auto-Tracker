# AGENTS.md

## Project Name

Vehicle Listing Event Pipeline with Kafka

## Purpose of This File

This file gives Codex and other AI coding agents clear instructions for working inside this repository.

Follow this file when generating code, modifying code, creating tests, updating documentation, or making architectural decisions for this project.

The stakeholder-facing source of truth is `docs/Project_Deliverables.md`. This `AGENTS.md` converts those deliverables into practical engineering instructions.

---

## Project Overview

Build a Spring Boot based event-driven backend application for vehicle listing history.

The system must include three main application components:

1. **producer-service**
   - Exposes REST endpoints.
   - Publishes vehicle listing events to Kafka.
   - Supports listing creation, price update, and sold events.

2. **consumer-service**
   - Consumes vehicle listing events from Kafka.
   - Validates incoming events.
   - Persists valid events to MySQL using Hibernate/JPA.
   - Sends malformed events to a Kafka dead-letter queue.
   - Exposes REST APIs to query listing history.

3. **frontend-ui**
   - Provides a simple user-facing interface for demoing the project.
   - Allows users to publish vehicle listing events through the producer API.
   - Allows users to view listing history through the consumer API.
   - Shows basic event flow status, such as created, price updated, sold, and malformed event handling.

The local infrastructure must also include **Kafka UI** for inspecting Kafka topics and DLQ messages during demos. Kafka UI is different from the user-facing frontend UI.

This project should demonstrate distributed architecture, asynchronous processing, Kafka event streaming, MySQL persistence, REST API design, DLQ handling, UI integration, and JUnit testing.

---

## Main Engineering Goal

Build a clean, portfolio-ready Kafka project that is simple enough to run locally but realistic enough to demonstrate production-style backend engineering.

The project should not be treated as a basic CRUD app. The main value is the producer-consumer architecture using Kafka.

---

## Required Tech Stack

Use the following technologies unless explicitly instructed otherwise:

- Java 17 or later
- Spring Boot
- Spring Kafka
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Apache Kafka
- Docker Compose
- Maven
- JUnit 5
- Mockito
- React
- TypeScript
- Vite or Next.js for the frontend UI

Required for local demo support:

- Kafka UI for inspecting Kafka topics and DLQ messages

Optional but allowed:

- Swagger/OpenAPI
- Testcontainers

Do not replace Kafka with RabbitMQ, ActiveMQ, Redis Streams, or another messaging system.

---

## Standard Local Ports

Use these ports unless explicitly changed:

| Component | Port |
|---|---:|
| Producer service | `8081` |
| Consumer service | `8082` |
| Frontend UI | `5173` |
| Kafka broker | `9092` |
| Kafka UI | `8080` |
| MySQL | `3306` |

## Expected Repository Structure

Use this structure unless the existing repository already has a clear structure.

```text
vehicle-listing-event-pipeline/
│
├── producer-service/
│   ├── src/main/java/...
│   ├── src/test/java/...
│   ├── pom.xml
│   └── README.md
│
├── consumer-service/
│   ├── src/main/java/...
│   ├── src/test/java/...
│   ├── pom.xml
│   └── README.md
│
├── frontend-ui/
│   ├── src/...
│   ├── package.json
│   ├── vite.config.ts or next.config.js
│   └── README.md
│
├── docker-compose.yml
├── README.md
├── AGENTS.md
└── docs/
    ├── Project_Deliverables.md
    ├── architecture.md
    ├── event-contract.md
    ├── api-examples.md
    ├── ui-guide.md
    └── demo-script.md
```

If creating a simpler version, it is acceptable to keep both services in one repository, but producer and consumer code should still be clearly separated.

---

## Service Boundaries

### Producer Service

The producer service is responsible only for receiving listing-related API requests and publishing events to Kafka.

It should not write listing history directly to MySQL.

Responsibilities:

- Accept REST requests.
- Validate basic request shape.
- Build event payloads.
- Publish events to Kafka topic `vehicle-listing-events`.
- Return clear API responses.

### Consumer Service

The consumer service is responsible for processing Kafka events and exposing listing history.

Responsibilities:

- Subscribe to `vehicle-listing-events`.
- Deserialize incoming events.
- Validate event fields.
- Save valid events to MySQL.
- Send invalid events to `vehicle-listing-events-dlq`.
- Expose REST APIs for querying listing history.
- Log processing success and failure cases.

### Frontend UI

The frontend UI is responsible for giving users and reviewers a simple way to interact with the event pipeline.

Responsibilities:

- Provide forms for publishing `LISTING_CREATED`, `PRICE_UPDATED`, and `LISTING_SOLD` events.
- Call the producer service APIs to publish events.
- Display listing history by calling the consumer service APIs.
- Provide a simple recent events view.
- Provide a listing-specific history view.
- Show basic success and error states for API calls.
- Keep the UI simple, clean, and focused on demonstrating the event-driven workflow.

The frontend should not talk directly to Kafka or MySQL. It should only communicate with backend REST APIs.

### Kafka UI

Kafka UI must be included in Docker Compose for local demos and topic inspection.

Use Kafka UI to show:

- Messages in `vehicle-listing-events`.
- Messages in `vehicle-listing-events-dlq`.
- Topic configuration and consumer activity.

Kafka UI is an infrastructure/observability tool, not the main user-facing frontend.

---

## Kafka Requirements

Use these topic names exactly:

```text
vehicle-listing-events
vehicle-listing-events-dlq
```

### Main Topic

Topic:

```text
vehicle-listing-events
```

Purpose:

- Carries valid vehicle listing lifecycle events.
- Used by the producer service to publish events.
- Used by the consumer service to receive events.

### Dead-Letter Queue Topic

Topic:

```text
vehicle-listing-events-dlq
```

Purpose:

- Stores malformed or unprocessable events.
- Must include enough information to understand why processing failed.

---

## Supported Event Types

The system must support the following event types:

```text
LISTING_CREATED
PRICE_UPDATED
LISTING_SOLD
```

Do not introduce additional event types unless requested.

---

## Vehicle Listing Event Contract

Use this event structure as the baseline Kafka payload.

```json
{
  "eventId": "evt-12345",
  "eventType": "LISTING_CREATED",
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": 24500.00,
  "previousPrice": null,
  "status": "ACTIVE",
  "dealerId": "DLR-2001",
  "eventTimestamp": "2026-04-30T14:25:00Z"
}
```

### Field Rules

| Field | Required | Notes |
|---|---:|---|
| `eventId` | Yes | Unique event identifier |
| `eventType` | Yes | Must be one of the supported event types |
| `listingId` | Yes | Unique listing identifier |
| `vin` | Conditional | Required for `LISTING_CREATED` |
| `make` | Conditional | Required for `LISTING_CREATED` |
| `model` | Conditional | Required for `LISTING_CREATED` |
| `year` | Conditional | Required for `LISTING_CREATED` |
| `price` | Conditional | Required for `LISTING_CREATED` and `PRICE_UPDATED` |
| `previousPrice` | No | Useful for `PRICE_UPDATED` |
| `status` | Yes | Listing state |
| `dealerId` | Yes | Dealer/source identifier |
| `eventTimestamp` | Yes | Original event timestamp |

---

## Listing Status Values

Use these status values:

```text
ACTIVE
UPDATED
SOLD
```

Expected usage:

- `LISTING_CREATED` should normally use `ACTIVE`.
- `PRICE_UPDATED` should normally use `UPDATED`.
- `LISTING_SOLD` should use `SOLD`.

---

## Producer API Requirements

The producer service should expose endpoints that allow testing event publication.

Suggested endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/listings/events/create` | Publish a listing created event |
| `POST` | `/api/listings/events/price-update` | Publish a price updated event |
| `POST` | `/api/listings/events/sold` | Publish a listing sold event |

### Producer Response Format

Return a clear response after publishing.

Example:

```json
{
  "status": "PUBLISHED",
  "topic": "vehicle-listing-events",
  "eventType": "PRICE_UPDATED",
  "listingId": "LST-1001",
  "message": "Vehicle listing event published successfully."
}
```

---

## Consumer API Requirements

The consumer service should expose APIs for querying listing history from MySQL.

Required endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/listings/history` | Get all listing history records |
| `GET` | `/api/listings/{listingId}/history` | Get history for a specific listing |
| `GET` | `/api/listings/history/event-type/{eventType}` | Get history by event type |
| `GET` | `/api/listings/history/dealer/{dealerId}` | Get history by dealer |
| `GET` | `/api/listings/history/recent` | Get recently processed events |

Use DTOs for API responses where useful.

Do not expose unnecessary internal database details.

---

## Frontend UI Requirements

The project must include a simple UI for demonstrating the complete workflow.

The UI should be practical and demo-friendly, not overly complex.

### Required UI Pages or Sections

| UI Area | Purpose |
|---|---|
| Dashboard | Show a high-level view of recent listing events |
| Create Listing Form | Publish a `LISTING_CREATED` event |
| Price Update Form | Publish a `PRICE_UPDATED` event |
| Sold Listing Form | Publish a `LISTING_SOLD` event |
| Listing History View | Query and display history for a specific listing ID |
| Recent Events View | Display recently processed listing events |
| Error/DLQ Demo Section | Allow sending or explaining malformed event behavior |

### UI Behavior

The UI should:

- Use the producer service for publishing listing events.
- Use the consumer service for reading listing history.
- Show loading states while API requests are running.
- Show success messages when events are published.
- Show error messages when backend calls fail.
- Keep forms simple and understandable.
- Use sample data where useful for easier demos.

### UI Technology Guidance

Use React with TypeScript.

Preferred setup:

```text
frontend-ui/
├── src/
│   ├── components/
│   ├── pages/ or routes/
│   ├── services/
│   ├── types/
│   └── App.tsx
├── package.json
└── README.md
```

Keep API calls in a dedicated service layer, such as:

```text
src/services/listingApi.ts
```

Define frontend types that match the backend event contract.

### UI API Integration

The frontend should call APIs like:

```text
POST /api/listings/events/create
POST /api/listings/events/price-update
POST /api/listings/events/sold
GET  /api/listings/{listingId}/history
GET  /api/listings/history/recent
```

If the frontend and backend run on different ports locally, configure CORS in the backend or use a frontend proxy.

### UI Acceptance Criteria

The UI is complete when a reviewer can:

1. Open the frontend locally.
2. Publish a listing created event.
3. Publish a price update event.
4. Publish a sold event.
5. Search for a listing ID and view its event history.
6. See clear success or error messages.
7. Understand the Kafka event pipeline from the UI flow.

---

## Database Requirements

Use MySQL as the main database.

Database name:

```text
vehicle_listing_db
```

Primary table:

```text
listing_history
```

### Required Columns

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key |
| `event_id` | VARCHAR(100) | Unique event ID |
| `event_type` | VARCHAR(50) | Event type |
| `listing_id` | VARCHAR(100) | Listing ID |
| `vin` | VARCHAR(50) | Vehicle VIN |
| `make` | VARCHAR(100) | Vehicle make |
| `model` | VARCHAR(100) | Vehicle model |
| `vehicle_year` | INT | Vehicle year |
| `price` | DECIMAL(12,2) | Current price |
| `previous_price` | DECIMAL(12,2) | Previous price |
| `status` | VARCHAR(50) | Listing status |
| `dealer_id` | VARCHAR(100) | Dealer ID |
| `event_timestamp` | DATETIME | Original event timestamp |
| `processed_at` | DATETIME | Consumer processing timestamp |

### Recommended Indexes

Create indexes for commonly queried fields:

- `listing_id`
- `event_type`
- `dealer_id`
- `event_timestamp`

Add a unique constraint on `event_id` if implementing idempotency.

---

## Validation Rules

Validate incoming events before saving them.

Reject events when:

- `eventId` is missing or blank.
- `eventType` is missing or unsupported.
- `listingId` is missing or blank.
- `dealerId` is missing or blank.
- `price` is negative.
- `eventTimestamp` is missing or invalid.
- `LISTING_CREATED` is missing vehicle details.
- `PRICE_UPDATED` is missing a new price.
- `LISTING_SOLD` does not represent a sold state.

Invalid events must not be saved to `listing_history`.

Invalid events must be sent to `vehicle-listing-events-dlq`.

---

## DLQ Message Contract

When sending an invalid event to the DLQ, include the original payload and reason for failure.

Example:

```json
{
  "originalPayload": "{ malformed event payload }",
  "errorReason": "Missing required field: listingId",
  "sourceTopic": "vehicle-listing-events",
  "failedAt": "2026-04-30T14:30:00Z"
}
```

The DLQ publisher should be simple and reliable.

---

## Consumer Processing Flow

Implement the consumer flow like this:

```text
Kafka event received
        |
        v
Deserialize JSON
        |
        v
Validate event
        |
        |--- valid ---> Save to MySQL
        |
        |--- invalid -> Publish to DLQ and log reason
```

The consumer must continue processing future events after a malformed event.

---

## Logging Requirements

Add meaningful logs for:

- Producer received request.
- Producer published event.
- Consumer received event.
- Consumer validation passed.
- Consumer validation failed.
- Consumer saved event to MySQL.
- Consumer sent event to DLQ.
- Unexpected errors.

Example log messages:

```text
INFO  Published event LISTING_CREATED for listingId=LST-1001 to topic=vehicle-listing-events
INFO  Consumed event PRICE_UPDATED for listingId=LST-1001
INFO  Persisted eventId=evt-12345 for listingId=LST-1001
WARN  Invalid event received. Reason=Missing listingId. Sending to DLQ.
```

Do not log secrets or database passwords.

---

## Configuration Requirements

Do not hardcode environment-specific values.

Use `application.yml`, environment variables, or Docker Compose configuration.

Expected configuration values:

```env
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_LISTING_TOPIC=vehicle-listing-events
KAFKA_LISTING_DLQ_TOPIC=vehicle-listing-events-dlq
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=vehicle_listing_db
MYSQL_USER=root
MYSQL_PASSWORD=password
```

Example Spring configuration:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: listing-history-consumer-group
  datasource:
    url: jdbc:mysql://localhost:3306/vehicle_listing_db
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

For local development, `ddl-auto: update` is acceptable.

For a more production-style implementation, prefer Flyway or Liquibase migrations.

---

## Docker Compose Requirements

Provide a `docker-compose.yml` file for local infrastructure.

At minimum, it must support:

- Kafka
- MySQL
- Kafka UI

Kafka UI is required because it helps reviewers inspect the main Kafka topic and the DLQ topic during demos.

The system should be runnable locally without requiring the user to install Kafka or MySQL manually.

---

## Build Commands

Use Maven unless the project has already been created with Gradle.

From each service directory:

```bash
mvn clean install
```

Run producer:

```bash
cd producer-service
mvn spring-boot:run
```

Run consumer:

```bash
cd consumer-service
mvn spring-boot:run
```

Run frontend UI:

```bash
cd frontend-ui
npm install
npm run dev
```

Run tests:

```bash
mvn test
```

Start infrastructure:

```bash
docker compose up -d
```

Stop infrastructure:

```bash
docker compose down
```

---

## Metrics and Observability Requirements

The consumer service should expose basic local metrics using Spring Boot Actuator and Micrometer.

Required local actuator endpoints:

- `/actuator/health`
- `/actuator/metrics`

Required custom metrics:

| Metric | Purpose |
|---|---|
| `vehicle_listing_events_consumed_total` | Counts events received by the consumer |
| `vehicle_listing_events_persisted_total` | Counts valid events saved to MySQL |
| `vehicle_listing_events_dlq_total` | Counts invalid events sent to the DLQ |
| `vehicle_listing_event_processing_latency` | Tracks event processing duration |

Metrics should be simple and local-development friendly.

Do not add Prometheus, Grafana, or external monitoring unless explicitly requested.

## Testing Requirements

Add JUnit tests for the consumer logic.

Minimum required test coverage:

1. A valid event is persisted successfully.
2. An invalid event is not persisted.
3. An invalid event is sent to the DLQ.

Suggested test class:

```text
VehicleListingConsumerTest
```

Suggested test methods:

```text
shouldPersistValidListingCreatedEvent
shouldPersistValidPriceUpdatedEvent
shouldSendMalformedEventToDlq
shouldRejectEventWithMissingListingId
shouldRejectNegativePrice
```

Use Mockito for unit tests.

Testcontainers may be added for integration tests, but do not make the project unnecessarily complex unless requested.

---

## Coding Style

Use clean Spring Boot layering.

Recommended packages:

```text
controller
service
kafka
dto
event
entity
repository
validator
config
exception
```

Guidelines:

- Keep code readable and beginner-friendly.
- Prefer clear names over overly abstract patterns.
- Avoid unnecessary complexity.
- Keep producer and consumer responsibilities separate.
- Use constructor injection.
- Avoid field injection.
- Keep validation logic separate from controller logic.
- Keep Kafka publishing logic separate from business logic.
- Use DTOs for requests and responses where helpful.
- Do not hardcode secrets.

---

## Documentation Requirements

Update or create documentation when adding functionality.

The root `README.md` should include:

- Project overview.
- Why Kafka is used.
- Architecture diagram or text flow.
- Technologies used.
- Local setup instructions.
- Docker Compose instructions.
- Producer API examples.
- Consumer API examples.
- Frontend UI setup and usage.
- Database schema.
- DLQ behavior.
- Testing instructions.
- Demo script.
- Future improvements.

The `docs/` folder should include, when possible:

- `architecture.md`
- `event-contract.md`
- `api-examples.md`
- `ui-guide.md`
- `demo-script.md`

---

## Demo Requirements

The project should support this demo flow:

1. Start Kafka, MySQL, and Kafka UI using Docker Compose.
2. Start the producer service.
3. Start the consumer service.
4. Start the frontend UI.
5. Use the UI to send a `LISTING_CREATED` event.
6. Use the UI to send a `PRICE_UPDATED` event.
7. Use the UI to send a `LISTING_SOLD` event.
8. Use the UI to query listing history for a listing ID.
9. Optionally use Kafka UI to show messages in the main Kafka topic.
10. Send a malformed event through an API call or demo control.
11. Confirm the malformed event was sent to the DLQ using logs or Kafka UI.
12. Run JUnit tests and show passing results.

---

## Acceptance Criteria

The implementation is complete when:

- Producer service starts successfully.
- Consumer service starts successfully.
- Kafka and MySQL run locally through Docker Compose.
- Producer publishes valid events to Kafka.
- Consumer receives events from Kafka.
- Consumer validates incoming events.
- Consumer saves valid events to MySQL.
- Consumer sends malformed events to the DLQ topic.
- REST API returns listing history from MySQL.
- Frontend UI allows publishing events and viewing listing history.
- Kafka UI is available for inspecting topics if included in Docker Compose.
- JUnit tests pass.
- README explains how to run and test the project, including the UI.

---

## Future Enhancements

The frontend UI is now part of the expected project scope. Do not treat it as optional.

Future enhancements that can be added after the base backend and UI are complete:

- Swagger/OpenAPI documentation.
- Testcontainers for Kafka and MySQL.
- Idempotency using unique `eventId`.
- Event schema versioning.
- More advanced Kafka UI configuration.
- Prometheus/Grafana integration for production-style monitoring.
- UI charts for event counts, DLQ counts, processing latency, and trends.
- Authentication for admin-only event publishing.

---

## Do Not Do

Do not:

- Replace Kafka with another broker.
- Skip DLQ handling.
- Save malformed events to the main listing history table.
- Let malformed events crash the consumer permanently.
- Hardcode secrets.
- Skip the frontend UI, because it is now part of the project scope.
- Over-engineer the project with unnecessary microservices.
- Remove existing working functionality without a clear reason.
- Ignore tests.
- Ignore documentation.

---

## Codex Working Instructions

When modifying this project:

1. Read `docs/Project_Deliverables.md` first.
2. Read this `AGENTS.md` file.
3. Preserve the producer-consumer architecture.
4. Make small, focused changes.
5. Add or update tests when behavior changes.
6. Update README or docs when setup, API, UI, topics, schema, or commands change.
7. Prefer simple working code over complex abstractions.
8. Make sure the project remains runnable locally.
9. Run relevant tests before considering the task complete.
10. Clearly explain any assumptions in code comments or documentation when needed.

---

## Final Intent

This repository should show that the developer understands modern backend architecture beyond basic CRUD.

The finished project should clearly demonstrate:

- Kafka producer-consumer design.
- Asynchronous processing.
- MySQL persistence.
- REST API development.
- Frontend UI integration.
- Dead-letter queue handling.
- Validation and error handling.
- JUnit testing.
- Clean documentation.
