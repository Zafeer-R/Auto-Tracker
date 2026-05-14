# Vehicle Listing Event Pipeline - MVP Phase Plan

This checklist breaks `docs/Project_Deliverables.md` into executable phases. Each phase has a concrete goal, detailed tasks, and a verification test. Do not move to the next phase until the current phase test passes.

The plan assumes the repository starts from scratch and currently contains `docs/Project_Deliverables.md` as the source requirements document.

> Note: This is an aggressive MVP implementation plan. If time becomes limited, prioritize the backend event flow, DLQ handling, REST API, frontend-ui demo flow, and core tests before advanced documentation polish or optional enhancements.

## Target Outcome

By the end of Day 2, the project should have:

- [X] A Spring Boot producer service that publishes vehicle listing events to Kafka.
- [X] A Spring Boot consumer service that consumes events, validates them, persists valid events to MySQL, and routes malformed events to a DLQ.
- [ ] Kafka, MySQL, and Kafka UI running locally through Docker Compose.
- [X] REST APIs for publishing events and querying listing history.
- [ ] Metrics for consumed, persisted, DLQ, and processing-latency behavior.
- [ ] A React dashboard that displays listing history and event status.
- [X] JUnit tests for the core consumer behavior.
- [ ] README and docs that make the project demo-ready.

## 2-Day Schedule

### Day 1

- [ ] Phase 0: Local setup requirements.
- [X] Phase 1: Repository structure and Docker infrastructure.
- [X] Phase 2: Event contract and documentation.
- [X] Phase 3: Producer service.
- [X] Phase 4: Consumer service, validation, and MySQL persistence.

### Day 2

- [X] Phase 5: Listing history REST API.
- [X] Phase 6: Dead-letter queue handling.
- [ ] Phase 7: Metrics and observability.
- [ ] Phase 8: React frontend dashboard.
- [ ] Phase 9: Automated tests.
- [ ] Phase 10: Documentation and final demo readiness.

## Standard Local Ports

- [X] Producer service: `8081`
- [X] Consumer service: `8082`
- [ ] React frontend: `5173`
- [ ] Kafka broker: `9092`
- [ ] Kafka UI: `8080`
- [ ] MySQL: `3306`

---

## Phase 0: Local Setup Requirements

Goal: Prepare your machine so the full backend, Kafka, MySQL, Kafka UI, and React dashboard can run locally.

Recommended timebox: 1 hour.

### Install Required Tools

- [X] Install Java 17 or newer.
- [X] Install Maven 3.9 or newer.
- [X] Install Docker Desktop.
- [X] Start Docker Desktop.
- [X] Install Node.js 20 or newer.
- [X] Confirm npm is installed with Node.
- [X] Install Git.
- [X] Install Postman, or confirm `curl` is available from the terminal.
- [X] Confirm no other local process is using ports `8080`, `8081`, `8082`, `5173`, `9092`, or `3306`.

### Verify Tooling

- [X] Open a terminal at the repository root.
- [X] Run `java -version`.
- [X] Confirm Java reports version 17 or newer.
- [X] Run `mvn -version`.
- [X] Confirm Maven runs successfully.
- [X] Run `docker --version`.
- [X] Confirm Docker runs successfully.
- [X] Run `docker compose version`.
- [X] Confirm Docker Compose runs successfully.
- [X] Run `node -v`.
- [X] Confirm Node reports version 20 or newer.
- [X] Run `npm -v`.
- [X] Confirm npm runs successfully.
- [X] Run `git --version`.
- [X] Confirm Git runs successfully.

### Create Local Environment Notes

- [X] Decide the local MySQL username. Recommended: `root`.
- [X] Decide the local MySQL password. Recommended for local only: `password`.
- [X] Use database name `vehicle_listing_db`.
- [X] Use Kafka main topic `vehicle-listing-events`.
- [X] Use Kafka DLQ topic `vehicle-listing-events-dlq`.
- [X] Use consumer group ID `listing-history-consumer-group`.

### Phase Test

- [X] `java -version` works and shows Java 17+.
- [X] `mvn -version` works.
- [X] `docker compose version` works.
- [X] `node -v` works and shows Node 20+.
- [X] `npm -v` works.
- [X] Docker Desktop shows the Docker engine is running.

Completion gate: local tooling is ready before any code is created.

---

## Phase 1: Repository Structure and Docker Infrastructure

Goal: Create the base project structure and local infrastructure for Kafka, MySQL, and Kafka UI.

Recommended timebox: 2 hours.

### Create Repository Structure

- [X] Create folder `producer-service`.
- [X] Create folder `consumer-service`.
- [X] Create folder `frontend-ui`.
- [X] Create folder `docs`.
- [X] Move or keep `Project_Deliverables.md` inside `docs/Project_Deliverables.md`.
- [X] Create folder `docs/postman` if you plan to export Postman collections.
- [X] Create folder `docs/api` for API documentation.
- [X] Create folder `docs/architecture` for architecture documentation.
- [X] Create folder `scripts` for helper scripts.
- [X] Keep `docs/Project_Deliverables.md` as the source requirements document.

Expected structure after this phase:

```text
VehicleListing/
|-- AGENTS.md
|-- phase.md
|-- docker-compose.yml
|-- README.md
|-- .env.example
|-- .gitignore
|-- producer-service/
|-- consumer-service/
|-- frontend-ui/
|-- scripts/
|-- docs/
    |-- Project_Deliverables.md
    |-- api/
    |-- architecture/
    |-- postman/
```

### Add Docker Compose

- [X] Create root `docker-compose.yml`.
- [X] Add Zookeeper service.
- [X] Set Zookeeper container name to `vehicle-listing-zookeeper`.
- [X] Expose Zookeeper port `2181:2181`.
- [X] Add MySQL service.
- [X] Set MySQL container name to `vehicle-listing-mysql`.
- [X] Set MySQL database to `vehicle_listing_db`.
- [X] Set MySQL root password to `password` for local development.
- [X] Expose MySQL port `3306:3306`.
- [X] Add Kafka service.
- [X] Configure Kafka to use Zookeeper instead of KRaft.
- [X] Configure Kafka to be reachable from host at `localhost:9092`.
- [X] Configure Kafka to be reachable from Docker containers at `kafka:29092`.
- [X] Expose Kafka port `9092:9092`.
- [X] Add Kafka UI service for Future Enhancement `27.5`.
- [X] Set Kafka UI container name to `vehicle-listing-kafka-ui`.
- [X] Expose Kafka UI port `8080:8080`.
- [X] Configure Kafka UI to connect to the Kafka container.
- [X] Add Docker volumes for MySQL, Kafka, and Zookeeper data.
- [X] Add MySQL health check.

### Create Kafka Topics

- [X] Ensure topic `vehicle-listing-events` is created.
- [X] Ensure topic `vehicle-listing-events-dlq` is created.
- [X] Configure both topics with at least 1 partition for local development.
- [X] Configure replication factor as 1 for local development.
- [X] Decide whether topics are created by Docker command, Kafka auto-create, or a topic-init container.
- [X] Prefer an explicit topic-init container so the setup is repeatable.
- [X] Add `kafka-init` service to create topics idempotently.

### Add Local Environment Documentation

- [X] Create `.env.example` at the root.
- [X] Add `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`.
- [X] Add `KAFKA_LISTING_TOPIC=vehicle-listing-events`.
- [X] Add `KAFKA_LISTING_DLQ_TOPIC=vehicle-listing-events-dlq`.
- [X] Add `MYSQL_HOST=localhost`.
- [X] Add `MYSQL_PORT=3306`.
- [X] Add `MYSQL_DATABASE=vehicle_listing_db`.
- [X] Add `MYSQL_USER=root`.
- [X] Add `MYSQL_PASSWORD=password`.
- [X] Add `PRODUCER_PORT=8081`.
- [X] Add `CONSUMER_PORT=8082`.
- [X] Add `FRONTEND_PORT=5173`.
- [X] Add README instructions to copy `.env.example` to `.env`.
- [X] Add PowerShell command `Copy-Item .env.example .env`.
- [X] Add Bash command `cp .env.example .env`.
- [X] Create root `.gitignore`.
- [X] Ignore `.env` in `.gitignore`.
- [X] Ignore Java, Node, IDE, log, OS, and local Docker generated files in `.gitignore`.

### Phase Test

- [X] Run `docker compose config`.
- [X] Run `docker compose up -d`.
- [X] Run `docker ps`.
- [X] Confirm Zookeeper container is running.
- [X] Confirm MySQL container is running.
- [X] Confirm Kafka container is running.
- [X] Confirm Kafka UI container is running.
- [X] Open `http://localhost:8080`.
- [X] Confirm Kafka UI loads.
- [X] Confirm Kafka UI can connect to the local Kafka cluster.
- [X] Confirm topic `vehicle-listing-events` exists.
- [X] Confirm topic `vehicle-listing-events-dlq` exists.
- [X] Confirm MySQL is reachable on port `3306`.
- [ ] If using MySQL CLI, connect with `mysql -h localhost -P 3306 -u root -p`.
- [X] Confirm database `vehicle_listing_db` exists.
- [X] Confirm `.env` is ignored by Git.

Completion gate: Kafka, Kafka UI, and MySQL run locally before service implementation begins.

---

## Phase 2: Event Contract and Documentation

Goal: Define the event schema used by the producer, Kafka topic, consumer, database mapping, API response, tests, and frontend-ui.

Recommended timebox: 1.5 hours.

### Create Event Contract Documentation

- [X] Create `docs/event-contract.md`.
- [X] Document the main Kafka topic as `vehicle-listing-events`.
- [X] Document the DLQ topic as `vehicle-listing-events-dlq`.
- [X] Document the purpose of each topic.
- [X] Document that events are serialized as JSON.
- [X] Document that timestamps use ISO-8601 format.
- [X] Document that money values use decimal numbers.

### Define Vehicle Listing Event Fields

- [X] Add field `eventId` as required string.
- [X] Add field `eventType` as required string.
- [X] Add field `listingId` as required string.
- [X] Add field `vin` as conditional string.
- [X] Add field `make` as conditional string.
- [X] Add field `model` as conditional string.
- [X] Add field `year` as conditional integer.
- [X] Add field `price` as conditional decimal.
- [X] Add field `previousPrice` as optional decimal.
- [X] Add field `status` as required string.
- [X] Add field `dealerId` as required string.
- [X] Add field `eventTimestamp` as required timestamp string.

### Define Supported Event Types

- [X] Document `LISTING_CREATED`.
- [X] Document `PRICE_UPDATED`.
- [X] Document `LISTING_SOLD`.
- [X] State that unsupported event types must be rejected by the consumer.

### Define Supported Status Values

- [X] Document `ACTIVE`.
- [X] Document `UPDATED`.
- [X] Document `SOLD`.
- [X] State that `LISTING_CREATED` should use status `ACTIVE`.
- [X] State that `PRICE_UPDATED` should use status `UPDATED`.
- [X] State that `LISTING_SOLD` should use status `SOLD`.

### Define Validation Rules

- [X] `eventId` must not be blank.
- [X] `eventType` must not be blank.
- [X] `eventType` must be one of the supported event types.
- [X] `listingId` must not be blank.
- [X] `dealerId` must not be blank.
- [X] `status` must not be blank.
- [X] `eventTimestamp` must not be blank.
- [X] `eventTimestamp` must parse as a valid timestamp.
- [X] `price` must not be negative when present.
- [X] `previousPrice` must not be negative when present.
- [X] `LISTING_CREATED` must include `vin`, `make`, `model`, `year`, and `price`.
- [X] `PRICE_UPDATED` must include `price`.
- [X] `LISTING_SOLD` must have status `SOLD`.

### Add Example Events

- [X] Add a valid `LISTING_CREATED` JSON example.
- [X] Add a valid `PRICE_UPDATED` JSON example.
- [X] Add a valid `LISTING_SOLD` JSON example.
- [X] Add an invalid event missing `listingId`.
- [X] Add an invalid event with negative `price`.
- [X] Add expected DLQ payload structure.

### Phase Test

- [X] Open `docs/event-contract.md`.
- [X] Confirm every event field from `Project_Deliverables.md` is documented.
- [X] Confirm all three supported event types are documented.
- [X] Confirm all validation rules are documented.
- [X] Confirm there is at least one valid event example.
- [X] Confirm there is at least one malformed event example.
- [X] Confirm the DLQ message shape includes `originalPayload`, `errorReason`, `sourceTopic`, and `failedAt`.

Completion gate: event schema is stable before producer and consumer models are created.

---

## Phase 3: Producer Service

Goal: Build the Spring Boot producer service that receives REST requests and publishes vehicle listing events to Kafka.

Recommended timebox: 4 hours.

### Create Spring Boot Project

- [X] Create a Spring Boot Maven project in `producer-service`.
- [X] Use Java 17.
- [X] Use package name `com.vehiclelisting.producer`.
- [X] Configure artifact name `producer-service`.
- [X] Add dependency `spring-boot-starter-web`.
- [X] Add dependency `spring-kafka`.
- [X] Add dependency `spring-boot-starter-validation`.
- [X] Add dependency `spring-boot-starter-test`.
- [X] Add Jackson dependencies only if not already included through Spring Boot.

### Configure Producer Application

- [X] Set server port to `8081`.
- [X] Configure Kafka bootstrap servers as `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`.
- [X] Configure listing topic as `${KAFKA_LISTING_TOPIC:vehicle-listing-events}`.
- [X] Configure JSON serialization for Kafka messages.
- [X] Create configuration class if needed for Kafka template setup.
- [X] Keep topic names externalized through application properties or YAML.

### Create Producer DTOs

- [X] Create request DTO for listing created.
- [X] Include `listingId`.
- [X] Include `vin`.
- [X] Include `make`.
- [X] Include `model`.
- [X] Include `year`.
- [X] Include `price`.
- [X] Include `dealerId`.
- [X] Create request DTO for price updated.
- [X] Include `listingId`.
- [X] Include `vin`.
- [X] Include `make`.
- [X] Include `model`.
- [X] Include `year`.
- [X] Include `previousPrice`.
- [X] Include `price`.
- [X] Include `dealerId`.
- [X] Create request DTO for listing sold.
- [X] Include `listingId`.
- [X] Include `vin`.
- [X] Include `make`.
- [X] Include `model`.
- [X] Include `year`.
- [X] Include `price`.
- [X] Include `dealerId`.
- [X] Add validation annotations for required request fields.

### Create Event Model

- [X] Create `VehicleListingEvent` model matching `docs/event-contract.md`.
- [X] Add `eventId`.
- [X] Add `eventType`.
- [X] Add `listingId`.
- [X] Add `vin`.
- [X] Add `make`.
- [X] Add `model`.
- [X] Add `year`.
- [X] Add `price`.
- [X] Add `previousPrice`.
- [X] Add `status`.
- [X] Add `dealerId`.
- [X] Add `eventTimestamp`.
- [X] Use `BigDecimal` for prices.
- [X] Use `Instant`, `OffsetDateTime`, or ISO string consistently for timestamps.

### Implement Event Creation Logic

- [X] Create service `VehicleListingEventFactory`.
- [X] Generate a unique `eventId` for every event.
- [X] Set `eventTimestamp` when the request is accepted.
- [X] For create requests, set `eventType` to `LISTING_CREATED`.
- [X] For create requests, set `status` to `ACTIVE`.
- [X] For price update requests, set `eventType` to `PRICE_UPDATED`.
- [X] For price update requests, set `status` to `UPDATED`.
- [X] For sold requests, set `eventType` to `LISTING_SOLD`.
- [X] For sold requests, set `status` to `SOLD`.

### Implement Kafka Publishing

- [X] Create service `VehicleListingPublisher`.
- [X] Inject `KafkaTemplate`.
- [X] Publish events to `vehicle-listing-events`.
- [X] Use `listingId` as the Kafka message key if possible.
- [X] Log topic, event type, event ID, and listing ID when publishing succeeds.
- [X] Log failures if publishing fails.
- [X] Return a clear API response even though Kafka processing is asynchronous.

### Implement REST Controller

- [X] Create controller `VehicleListingEventController`.
- [X] Add `POST /api/listings/events/create`.
- [X] Add `POST /api/listings/events/price-update`.
- [X] Add `POST /api/listings/events/sold`.
- [X] Validate request bodies with `@Valid`.
- [X] Return HTTP `202 Accepted` for successfully accepted publish requests.
- [X] Return HTTP `400 Bad Request` for invalid request bodies.
- [X] Return response field `status`.
- [X] Return response field `topic`.
- [X] Return response field `eventType`.
- [X] Return response field `listingId`.
- [X] Return response field `message`.

### Add Producer Logging

- [X] Log when a create request is received.
- [X] Log when a price update request is received.
- [X] Log when a sold request is received.
- [X] Log when an event is published.
- [X] Avoid logging sensitive configuration values.

### Phase Test

- [X] Start Docker infrastructure with `docker compose up -d`.
- [X] Start producer service from `producer-service` using `mvn spring-boot:run`.
- [X] Send a create request to `http://localhost:8081/api/listings/events/create`.
- [X] Confirm response status is `202`.
- [X] Confirm response contains `status`, `topic`, `eventType`, `listingId`, and `message`.
- [X] Open Kafka UI at `http://localhost:8080`.
- [X] Confirm a `LISTING_CREATED` message appears in `vehicle-listing-events`.
- [X] Send a price update request to `http://localhost:8081/api/listings/events/price-update`.
- [X] Confirm a `PRICE_UPDATED` message appears in Kafka UI.
- [X] Send a sold request to `http://localhost:8081/api/listings/events/sold`.
- [X] Confirm a `LISTING_SOLD` message appears in Kafka UI.
- [X] Send a request missing a required field.
- [X] Confirm the producer returns HTTP `400`.

Completion gate: producer can publish all three event types to Kafka.

---

## Phase 4: Consumer Service, Validation, and MySQL Persistence

Goal: Build the Spring Boot consumer service that reads Kafka events, validates them, and persists valid events to MySQL.

Recommended timebox: 5 hours.

### Create Spring Boot Project

- [X] Create a Spring Boot Maven project in `consumer-service`.
- [X] Use Java 17.
- [X] Use package name `com.vehiclelisting.consumer`.
- [X] Configure artifact name `consumer-service`.
- [X] Add dependency `spring-boot-starter-web`.
- [X] Add dependency `spring-kafka`.
- [X] Add dependency `spring-boot-starter-data-jpa`.
- [X] Add dependency `mysql-connector-j`.
- [X] Add dependency `spring-boot-starter-validation`.
- [X] Add dependency `spring-boot-starter-actuator`.
- [X] Add dependency `micrometer-core`.
- [X] Add dependency `spring-boot-starter-test`.
- [X] Add dependency `mockito-core` if not already available through tests.

### Configure Consumer Application

- [X] Set server port to `8082`.
- [X] Configure Kafka bootstrap servers as `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`.
- [X] Configure consumer group ID as `${KAFKA_CONSUMER_GROUP_ID:listing-history-consumer-group}`.
- [X] Configure main topic as `${KAFKA_LISTING_TOPIC:vehicle-listing-events}`.
- [X] Configure DLQ topic as `${KAFKA_LISTING_DLQ_TOPIC:vehicle-listing-events-dlq}`.
- [X] Configure MySQL URL as `jdbc:mysql://localhost:3306/vehicle_listing_db`.
- [X] Configure MySQL username as `${MYSQL_USER:root}`.
- [X] Configure MySQL password as `${MYSQL_PASSWORD:password}`.
- [X] Configure Hibernate DDL for local development as `update`.
- [X] Configure JSON deserialization for Kafka messages.

### Create Consumer Event Model

- [X] Create `VehicleListingEvent` model matching the event contract.
- [X] Use the same field names as producer JSON.
- [X] Use `BigDecimal` for prices.
- [X] Use a timestamp type that can parse `eventTimestamp`.
- [X] Confirm Jackson can deserialize the model from Kafka JSON.

### Create Database Entity

- [X] Create entity `ListingHistory`.
- [X] Map table name `listing_history`.
- [X] Add primary key `id` as auto-generated `BIGINT`.
- [X] Add column `event_id`.
- [X] Add column `event_type`.
- [X] Add column `listing_id`.
- [X] Add column `vin`.
- [X] Add column `make`.
- [X] Add column `model`.
- [X] Add column `vehicle_year`.
- [X] Add column `price`.
- [X] Add column `previous_price`.
- [X] Add column `status`.
- [X] Add column `dealer_id`.
- [X] Add column `event_timestamp`.
- [X] Add column `processed_at`.
- [X] Add index on `listing_id`.
- [X] Add index on `event_type`.
- [X] Add index on `dealer_id`.
- [X] Add index on `event_timestamp`.
- [X] Add unique constraint on `event_id` if you choose to include idempotency from the risks section.

### Create Repository

- [X] Create `ListingHistoryRepository`.
- [X] Extend `JpaRepository<ListingHistory, Long>`.
- [X] Add method to find by `listingId`.
- [X] Add method to find by `eventType`.
- [X] Add method to find by `dealerId`.
- [X] Add method to find recent events sorted by processed time.

### Implement Validation

- [X] Create `VehicleListingEventValidator`.
- [X] Reject blank `eventId`.
- [X] Reject blank `eventType`.
- [X] Reject unsupported `eventType`.
- [X] Reject blank `listingId`.
- [X] Reject blank `dealerId`.
- [X] Reject blank `status`.
- [X] Reject blank or invalid `eventTimestamp`.
- [X] Reject negative `price`.
- [X] Reject negative `previousPrice`.
- [X] Reject `LISTING_CREATED` if `vin`, `make`, `model`, `year`, or `price` is missing.
- [X] Reject `PRICE_UPDATED` if `price` is missing.
- [X] Reject `LISTING_SOLD` if `status` is not `SOLD`.
- [X] Return a clear error reason for every validation failure.

### Implement Persistence Mapping

- [X] Create mapper from `VehicleListingEvent` to `ListingHistory`.
- [X] Copy event fields into entity fields.
- [X] Set `processedAt` to current time when saving.
- [X] Preserve original `eventTimestamp` from the event.

### Implement Kafka Listener

- [X] Create consumer class `VehicleListingConsumer`.
- [X] Listen to topic `vehicle-listing-events`.
- [X] Log when a raw event is received.
- [X] Deserialize JSON into `VehicleListingEvent`.
- [X] Validate the event.
- [X] Persist valid events.
- [X] Log successful persistence with `eventId` and `listingId`.
- [X] Catch unexpected processing errors.
- [X] Keep the consumer running after failures.

### Phase Test

- [X] Start Docker infrastructure.
- [X] Start consumer service from `consumer-service` using `mvn spring-boot:run`.
- [X] Start producer service.
- [X] Publish a valid `LISTING_CREATED` event.
- [X] Confirm consumer logs show the event was received.
- [X] Confirm consumer logs show validation passed.
- [X] Confirm consumer logs show the event was persisted.
- [X] Query MySQL table `listing_history`.
- [X] Confirm one row exists for the published `listingId`.
- [X] Publish `PRICE_UPDATED`.
- [X] Confirm a second row exists for the same `listingId`.
- [X] Publish `LISTING_SOLD`.
- [X] Confirm a third row exists for the same `listingId`.

Completion gate: valid events flow from producer to Kafka to consumer to MySQL.

---

## Phase 5: Listing History REST API

Goal: Expose query APIs from the consumer service so saved listing history can be retrieved by clients and the frontend-ui.

Recommended timebox: 3 hours.

### Create API Response DTO

- [X] Create `ListingHistoryResponse`.
- [X] Include `eventId`.
- [X] Include `eventType`.
- [X] Include `listingId`.
- [X] Include `vin`.
- [X] Include `make`.
- [X] Include `model`.
- [X] Include `year`.
- [X] Include `price`.
- [X] Include `previousPrice`.
- [X] Include `status`.
- [X] Include `dealerId`.
- [X] Include `eventTimestamp`.
- [X] Include `processedAt`.
- [X] Avoid returning raw JPA entities directly.

### Create Query Service

- [X] Create `ListingHistoryService`.
- [X] Add method to get all history.
- [X] Add method to get history by listing ID.
- [X] Add method to get history by event type.
- [X] Add method to get history by dealer ID.
- [X] Add method to get recent history.
- [X] Sort results by event timestamp or processed timestamp.
- [X] Convert entities to response DTOs.

### Create REST Controller

- [X] Create `ListingHistoryController`.
- [X] Add `GET /api/listings/history`.
- [X] Add `GET /api/listings/{listingId}/history`.
- [X] Add `GET /api/listings/history/event-type/{eventType}`.
- [X] Add `GET /api/listings/history/dealer/{dealerId}`.
- [X] Add `GET /api/listings/history/recent`.
- [X] Return JSON arrays.
- [X] Return empty arrays when no data matches.
- [X] Return HTTP `200` for successful queries.
- [X] Return HTTP `400` for invalid event type filters if validation is added.

### Add CORS For Frontend

- [X] Allow requests from `http://localhost:5173`.
- [X] Restrict CORS to local development configuration.
- [X] Confirm frontend-ui can call consumer APIs without browser CORS errors.

### Document API Examples

- [X] Create `docs/api-examples.md`.
- [X] Add example request for all history.
- [X] Add example request for listing ID history.
- [X] Add example request for event type filter.
- [X] Add example request for dealer filter.
- [X] Add example request for recent history.
- [X] Add sample JSON response.

### Phase Test

- [X] Publish at least three valid events for listing `LST-1001`.
- [X] Call `GET http://localhost:8082/api/listings/history`.
- [X] Confirm all persisted records are returned.
- [X] Call `GET http://localhost:8082/api/listings/LST-1001/history`.
- [X] Confirm only records for `LST-1001` are returned.
- [X] Call `GET http://localhost:8082/api/listings/history/event-type/PRICE_UPDATED`.
- [X] Confirm only price update records are returned.
- [X] Call `GET http://localhost:8082/api/listings/history/dealer/DLR-2001`.
- [X] Confirm only records for dealer `DLR-2001` are returned.
- [X] Call `GET http://localhost:8082/api/listings/history/recent`.
- [X] Confirm the latest records are returned.

Completion gate: consumer REST API can query persisted listing history.

---

## Phase 6: Dead-Letter Queue Handling

Goal: Ensure malformed events do not crash the consumer and are sent to the DLQ topic for inspection.

Recommended timebox: 3 hours.

### Create DLQ Message Contract

- [X] Create `DlqMessage` model.
- [X] Add field `originalPayload`.
- [X] Add field `errorReason`.
- [X] Add field `sourceTopic`.
- [X] Add field `failedAt`.
- [X] Use source topic value `vehicle-listing-events`.
- [X] Use DLQ topic value `vehicle-listing-events-dlq`.

### Implement DLQ Publisher

- [X] Create service `DlqPublisher`.
- [X] Inject `KafkaTemplate`.
- [X] Publish DLQ messages to `vehicle-listing-events-dlq`.
- [X] Include the original raw payload whenever possible.
- [X] Include a human-readable error reason.
- [X] Include failure timestamp.
- [X] Log successful DLQ publication.
- [X] Log DLQ publication failures.

### Update Consumer Error Handling

- [X] Catch JSON deserialization errors.
- [X] Send invalid JSON to DLQ.
- [X] Catch validation errors.
- [X] Send validation failures to DLQ.
- [X] Catch database save failures.
- [X] Log database save failures.
- [X] Decide whether database failures should go to DLQ or be retried. For this 2-day project, send to DLQ with the error reason.
- [X] Ensure invalid events are never saved to MySQL.
- [X] Ensure a malformed event does not stop the listener permanently.
- [X] Ensure the next valid event is still processed after a bad event.

### Add Malformed Event Test Inputs

- [X] Create sample event missing `listingId`.
- [X] Create sample event with unsupported `eventType`.
- [X] Create sample event with negative `price`.
- [X] Create sample event with invalid timestamp.
- [X] Create sample invalid JSON payload.
- [X] Store examples in `docs/api-examples.md` or `docs/demo-script.md`.

### Phase Test

- [X] Send an event missing `listingId` to Kafka.
- [X] Confirm no new row is added to MySQL.
- [X] Open Kafka UI.
- [X] Confirm a message appears in `vehicle-listing-events-dlq`.
- [X] Confirm the DLQ message includes `originalPayload`.
- [X] Confirm the DLQ message includes `errorReason`.
- [X] Confirm the DLQ message includes `sourceTopic`.
- [X] Confirm the DLQ message includes `failedAt`.
- [X] Send an event with negative `price`.
- [X] Confirm it goes to DLQ.
- [X] Send a valid event after the malformed events.
- [X] Confirm the valid event is persisted successfully.

Completion gate: bad events go to DLQ and good events continue processing afterward.

---

## Phase 7: Metrics and Observability

Goal: Add operational visibility from Future Enhancement `27.6`.

Recommended timebox: 2 hours.

### Enable Actuator

- [ ] Enable Spring Boot Actuator in `consumer-service`.
- [ ] Expose `/actuator/health`.
- [ ] Expose `/actuator/metrics`.
- [ ] Configure actuator exposure only for local development.
- [ ] Confirm service health reports `UP`.

### Add Custom Metrics

- [ ] Add counter `vehicle_listing_events_consumed_total`.
- [ ] Increment consumed counter when an event is received.
- [ ] Add counter `vehicle_listing_events_persisted_total`.
- [ ] Increment persisted counter after successful database save.
- [ ] Add counter `vehicle_listing_events_dlq_total`.
- [ ] Increment DLQ counter when an event is routed to DLQ.
- [ ] Add timer `vehicle_listing_event_processing_latency`.
- [ ] Record processing duration for each consumed event.

### Improve Logs

- [ ] Producer logs event request received.
- [ ] Producer logs event published to Kafka.
- [ ] Consumer logs event received.
- [ ] Consumer logs event validation success.
- [ ] Consumer logs event validation failure.
- [ ] Consumer logs event persisted to MySQL.
- [ ] Consumer logs event sent to DLQ.
- [ ] Consumer logs unexpected processing errors.
- [ ] Include `eventId`, `listingId`, and `eventType` in logs when available.

### Document Observability

- [ ] Add metrics endpoint details to README.
- [ ] Add example health endpoint call.
- [ ] Add example metrics endpoint calls.
- [ ] Explain how to use Kafka UI to inspect main and DLQ topics.

### Phase Test

- [ ] Start consumer service.
- [ ] Open `http://localhost:8082/actuator/health`.
- [ ] Confirm response status is `UP`.
- [ ] Publish a valid event.
- [ ] Open `http://localhost:8082/actuator/metrics`.
- [ ] Confirm consumed counter exists.
- [ ] Confirm persisted counter exists.
- [ ] Publish an invalid event.
- [ ] Confirm DLQ counter exists.
- [ ] Confirm logs clearly show the event lifecycle.

Completion gate: event flow can be observed through logs, metrics, and Kafka UI.

---

## Phase 8: React Frontend UI Dashboard

Goal: Build a simple React dashboard from Future Enhancement `27.7` to show listing history and event status.

Recommended timebox: 4 hours.

### Create Frontend Project

- [ ] Create a Vite React project inside `frontend-ui`.
- [ ] Use TypeScript to align with `AGENTS.md`.
- [ ] Configure dev server port as `5173`.
- [ ] Install dependencies.
- [ ] Confirm `npm run dev` starts the app.
- [ ] Remove starter content.

### Configure API Client

- [ ] Create API base URL config.
- [ ] Default consumer API URL to `http://localhost:8082`.
- [ ] Create function to fetch all listing history.
- [ ] Create function to fetch history by listing ID.
- [ ] Create function to fetch history by event type.
- [ ] Create function to fetch history by dealer ID.
- [ ] Create function to fetch recent history.
- [ ] Create function to publish `LISTING_CREATED` through the producer API.
- [ ] Create function to publish `PRICE_UPDATED` through the producer API.
- [ ] Create function to publish `LISTING_SOLD` through the producer API.
- [ ] Create function to fetch metrics if you expose usable metrics data.

### Build Dashboard Layout

- [ ] Add page title `Vehicle Listing Event Dashboard`.
- [ ] Add short subtitle explaining Kafka event history.
- [ ] Add summary card for total events.
- [ ] Add summary card for recent events.
- [ ] Add summary card for event types represented.
- [ ] Add summary card for DLQ count if available.
- [ ] Add main listing history table.
- [ ] Add refresh button.
- [ ] Add visible last refreshed timestamp.


### Build Event Publishing Forms

- [ ] Add Create Listing form.
- [ ] Add Price Update form.
- [ ] Add Sold Listing form.
- [ ] Connect Create Listing form to the producer API.
- [ ] Connect Price Update form to the producer API.
- [ ] Connect Sold Listing form to the producer API.
- [ ] Show success message after an event is published.
- [ ] Show error message if the producer API call fails.
- [ ] Use sample default values where helpful for a faster demo.

### Build Filters

- [ ] Add listing ID filter input.
- [ ] Add event type dropdown.
- [ ] Include option for all event types.
- [ ] Include option `LISTING_CREATED`.
- [ ] Include option `PRICE_UPDATED`.
- [ ] Include option `LISTING_SOLD`.
- [ ] Add dealer ID filter input.
- [ ] Add clear filters button.
- [ ] Make filters call the correct consumer API endpoint or filter locally.

### Build Listing History Table

- [ ] Show column `Event Type`.
- [ ] Show column `Listing ID`.
- [ ] Show column `VIN`.
- [ ] Show column `Make`.
- [ ] Show column `Model`.
- [ ] Show column `Year`.
- [ ] Show column `Price`.
- [ ] Show column `Previous Price`.
- [ ] Show column `Status`.
- [ ] Show column `Dealer ID`.
- [ ] Show column `Event Timestamp`.
- [ ] Show column `Processed At`.
- [ ] Format prices as currency.
- [ ] Format timestamps for readability.
- [ ] Add visual status labels for `ACTIVE`, `UPDATED`, and `SOLD`.

### Add UI States

- [ ] Show loading state while fetching records.
- [ ] Show empty state when no records exist.
- [ ] Show error state when the consumer API is unavailable.
- [ ] Show helpful message if Docker/backend services are not running.
- [ ] Make layout usable on desktop.
- [ ] Make layout usable on mobile.

### Connect Full Flow

- [ ] Start Docker infrastructure.
- [ ] Start producer service.
- [ ] Start consumer service.
- [ ] Start frontend-ui service.
- [ ] Publish sample events through producer API.
- [ ] Refresh dashboard.
- [ ] Confirm dashboard data updates.

### Phase Test

- [ ] Run `npm run dev` inside `frontend-ui`.
- [ ] Open `http://localhost:5173`.
- [ ] Confirm dashboard loads.
- [ ] Confirm empty state appears if no events exist.
- [ ] Publish a `LISTING_CREATED` event from the UI.
- [ ] Refresh dashboard if needed.
- [ ] Confirm the event appears in the table.
- [ ] Publish `PRICE_UPDATED` and `LISTING_SOLD` events from the UI.
- [ ] Confirm all events appear in the table.
- [ ] Filter by listing ID.
- [ ] Confirm only matching listing rows appear.
- [ ] Filter by event type.
- [ ] Confirm only matching event type rows appear.
- [ ] Stop consumer service.
- [ ] Refresh dashboard.
- [ ] Confirm error state appears.

Completion gate: reviewer can visually inspect listing history through the React dashboard.

---

## Phase 9: Automated Tests

Goal: Add tests that prove the most important backend behavior works.

Recommended timebox: 3 hours.

### Producer Tests

- [ ] Add test for create request validation if time allows.
- [ ] Add test for price update request validation if time allows.
- [ ] Add test for sold request validation if time allows.
- [ ] Add test that event factory creates `LISTING_CREATED` with status `ACTIVE`.
- [ ] Add test that event factory creates `PRICE_UPDATED` with status `UPDATED`.
- [ ] Add test that event factory creates `LISTING_SOLD` with status `SOLD`.

### Consumer Validator Tests

- [ ] Add test that valid listing created event passes validation.
- [ ] Add test that missing `eventId` fails validation.
- [ ] Add test that missing `listingId` fails validation.
- [ ] Add test that unsupported `eventType` fails validation.
- [ ] Add test that negative `price` fails validation.
- [ ] Add test that invalid timestamp fails validation.
- [ ] Add test that `LISTING_CREATED` without vehicle details fails validation.
- [ ] Add test that `PRICE_UPDATED` without price fails validation.
- [ ] Add test that `LISTING_SOLD` with non-sold status fails validation.

### Consumer Processing Tests

- [ ] Create test class `VehicleListingConsumerTest`.
- [ ] Mock `ListingHistoryRepository`.
- [ ] Mock `DlqPublisher`.
- [ ] Mock metrics dependencies if needed.
- [ ] Add test `shouldPersistValidListingCreatedEvent`.
- [ ] Verify repository `save` is called once.
- [ ] Verify DLQ publisher is not called.
- [ ] Add test `shouldPersistValidPriceUpdatedEvent`.
- [ ] Verify repository `save` is called once.
- [ ] Add test `shouldSendMalformedEventToDlq`.
- [ ] Verify repository `save` is never called.
- [ ] Verify DLQ publisher is called once.
- [ ] Add test `shouldRejectEventWithMissingListingId`.
- [ ] Verify DLQ publisher receives an error reason mentioning `listingId`.
- [ ] Add test `shouldRejectNegativePrice`.
- [ ] Verify DLQ publisher receives an error reason mentioning `price`.

### API Tests If Time Allows

- [ ] Add controller test for `GET /api/listings/history`.
- [ ] Add controller test for listing ID filter.
- [ ] Add controller test for event type filter.
- [ ] Add controller test for dealer filter.

### Frontend Tests If Time Allows

- [ ] Add a smoke test that dashboard renders.
- [ ] Add a test that empty state renders.
- [ ] Add a test that table renders when API returns events.

### Phase Test

- [ ] Run `mvn test` inside `producer-service`.
- [ ] Confirm producer tests pass.
- [ ] Run `mvn test` inside `consumer-service`.
- [ ] Confirm consumer tests pass.
- [ ] Confirm consumer tests cover valid save behavior.
- [ ] Confirm consumer tests cover invalid DLQ behavior.
- [ ] If frontend-ui tests exist, run the configured frontend-ui test command.
- [ ] Confirm test commands are documented in README.

Completion gate: core event processing behavior is protected by automated tests.

---

## Phase 10: Documentation and Final Demo Readiness

Goal: Make the project portfolio-ready and easy for a reviewer to run from scratch.

Recommended timebox: 3 hours.

### Create Root README

- [ ] Create root `README.md`.
- [ ] Add project title.
- [ ] Add project summary.
- [ ] Explain the business problem.
- [ ] Explain why Kafka is used.
- [ ] Add architecture overview.
- [ ] Add text architecture diagram.
- [ ] Add technology stack.
- [ ] Add repository structure.
- [ ] Add local setup requirements.
- [ ] Add environment variable table.
- [ ] Add Docker startup instructions.
- [ ] Add producer startup instructions.
- [ ] Add consumer startup instructions.
- [ ] Add frontend-ui startup instructions.
- [ ] Add Kafka UI instructions.
- [ ] Add MySQL instructions.
- [ ] Add testing instructions.
- [ ] Add troubleshooting notes.
- [ ] Add future improvements section.

### Create Architecture Documentation

- [ ] Create `docs/architecture.md`.
- [ ] Explain producer responsibility.
- [ ] Explain Kafka topic responsibility.
- [ ] Explain consumer responsibility.
- [ ] Explain MySQL persistence responsibility.
- [ ] Explain DLQ responsibility.
- [ ] Explain frontend dashboard responsibility.
- [ ] Include text diagram from client to producer to Kafka to consumer to MySQL/API/frontend-ui.

### Finalize API Documentation

- [ ] Update `docs/api-examples.md`.
- [ ] Add create listing curl example.
- [ ] Add price update curl example.
- [ ] Add sold listing curl example.
- [ ] Add all history curl example.
- [ ] Add listing ID history curl example.
- [ ] Add event type history curl example.
- [ ] Add dealer history curl example.
- [ ] Add recent history curl example.
- [ ] Add malformed event example.
- [ ] Add expected DLQ output example.

### Create Demo Script

- [ ] Create `docs/demo-script.md`.
- [ ] Add step 1: start Docker with `docker compose up -d`.
- [ ] Add step 2: open Kafka UI at `http://localhost:8080`.
- [ ] Add step 3: start producer service.
- [ ] Add step 4: start consumer service.
- [ ] Add step 5: start React frontend.
- [ ] Add step 6: publish `LISTING_CREATED`.
- [ ] Add step 7: publish `PRICE_UPDATED`.
- [ ] Add step 8: publish `LISTING_SOLD`.
- [ ] Add step 9: query listing history API.
- [ ] Add step 10: show listing history in frontend-ui.
- [ ] Add step 11: send malformed event.
- [ ] Add step 12: show malformed event in Kafka UI DLQ topic.
- [ ] Add step 13: open metrics endpoint.
- [ ] Add step 14: run JUnit tests.

### Final End-to-End Test

- [ ] Stop all running app services.
- [ ] Run `docker compose down`.
- [ ] Run `docker compose up -d`.
- [ ] Start producer service.
- [ ] Start consumer service.
- [ ] Start frontend-ui.
- [ ] Publish `LISTING_CREATED` from the UI.
- [ ] Publish `PRICE_UPDATED` from the UI.
- [ ] Publish `LISTING_SOLD` from the UI.
- [ ] Confirm Kafka UI shows events in `vehicle-listing-events`.
- [ ] Confirm MySQL contains rows in `listing_history`.
- [ ] Confirm `GET /api/listings/LST-1001/history` returns all listing events.
- [ ] Confirm frontend-ui displays all listing events.
- [ ] Send malformed event.
- [ ] Confirm Kafka UI shows message in `vehicle-listing-events-dlq`.
- [ ] Confirm metrics endpoint exposes consumed, persisted, and DLQ counts.
- [ ] Run producer tests.
- [ ] Run consumer tests.
- [ ] Confirm README instructions match the actual commands.

### Phase Test

- [ ] A reviewer can follow README from a clean terminal.
- [ ] Docker infrastructure starts successfully.
- [ ] Producer starts successfully.
- [ ] Consumer starts successfully.
- [ ] Frontend starts successfully.
- [ ] Valid events are published, consumed, persisted, queried, and displayed.
- [ ] Invalid events are sent to DLQ.
- [ ] Metrics are visible.
- [ ] Tests pass.
- [ ] Demo script is complete.

Completion gate: the project is ready for portfolio, internship, or interview demonstration.

---

## Final Definition of Done

The project is done when all of these are complete:

- [ ] `docker compose up -d` starts Kafka, MySQL, and Kafka UI.
- [ ] Kafka UI is reachable at `http://localhost:8080`.
- [ ] Main Kafka topic `vehicle-listing-events` exists.
- [ ] DLQ Kafka topic `vehicle-listing-events-dlq` exists.
- [X] Producer service starts on port `8081`.
- [X] Consumer service starts on port `8082`.
- [ ] Frontend dashboard starts on port `5173`.
- [X] Producer publishes `LISTING_CREATED`.
- [X] Producer publishes `PRICE_UPDATED`.
- [X] Producer publishes `LISTING_SOLD`.
- [X] Consumer consumes valid events.
- [X] Consumer validates event fields.
- [X] Consumer saves valid events to MySQL.
- [X] Consumer rejects malformed events.
- [X] Consumer sends malformed events to DLQ.
- [X] Listing history API returns all saved records.
- [X] Listing history API filters by listing ID.
- [X] Listing history API filters by event type.
- [X] Listing history API filters by dealer.
- [X] Listing history API returns recent records.
- [ ] React dashboard allows publishing listing events through the producer API.
- [ ] React dashboard displays listing history.
- [ ] React dashboard supports filtering.
- [ ] React dashboard has loading, empty, and error states.
- [ ] Metrics endpoint shows consumed event count.
- [ ] Metrics endpoint shows persisted event count.
- [ ] Metrics endpoint shows DLQ event count.
- [ ] Metrics endpoint shows processing latency.
- [ ] Producer tests pass.
- [X] Consumer tests pass.
- [ ] README explains how to run the project.
- [ ] Documentation includes architecture, API examples, event contract, and demo script.
- [ ] The final demo can be completed end-to-end in under 10 minutes.
