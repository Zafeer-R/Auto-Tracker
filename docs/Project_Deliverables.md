# Vehicle Listing Event Pipeline with Kafka — Deliverables Document

## 1. Executive Summary

This project delivers a Spring Boot based event-driven vehicle listing pipeline that demonstrates how vehicle listing changes can be published, processed asynchronously, persisted, and queried through an API.

The system contains two main services:

1. **Listing Producer Service**  
   Publishes vehicle listing events to Kafka when important listing actions occur, such as a new vehicle lot being added, a price being updated, or a vehicle being sold.

2. **Listing Consumer Service**  
   Consumes those Kafka events asynchronously, validates them, persists valid events to MySQL using Hibernate/JPA, exposes a REST API for querying listing history, and sends malformed events to a dead-letter queue.

This project is designed to demonstrate practical understanding of distributed systems, asynchronous processing, Kafka-based architecture, database persistence, REST API design, and testable backend engineering.

---

## 2. Business Context

Vehicle listing platforms need to track changes over time. A listing may be created, updated, repriced, sold, removed, or corrected. In a real marketplace, these events may come from many sources, including dealer portals, internal admin tools, pricing engines, inventory feeds, or partner APIs.

Instead of tightly coupling every system together, this project uses Kafka as the event backbone. This allows listing changes to be published once and processed independently by downstream services.

For stakeholders, this means the system is:

- More scalable than a direct synchronous request-response design.
- More resilient because events can be retried or redirected to a dead-letter queue.
- Easier to extend because future consumers can be added without changing the producer.
- More aligned with modern distributed application architecture.

---

## 3. Project Goal

Build a working event-driven backend system where vehicle listing events are published to Kafka, consumed asynchronously, saved to MySQL, and made available through a REST API.

The project should be suitable for a technical portfolio, internship application, or interview demonstration. It should clearly show knowledge of Kafka, Spring Boot, Hibernate/JPA, MySQL, REST API design, and automated testing.

---

## 4. Core Use Cases

### 4.1 New Listing Added

A producer publishes an event when a new vehicle listing is created.

Example event type:

```text
LISTING_CREATED
```

Expected outcome:

- Event is published to Kafka.
- Consumer receives the event.
- Event is validated.
- Listing history record is saved in MySQL.
- Listing history can be retrieved through the REST API.

---

### 4.2 Listing Price Updated

A producer publishes an event when the vehicle price changes.

Example event type:

```text
PRICE_UPDATED
```

Expected outcome:

- Price update event is published to Kafka.
- Consumer stores the price change as a historical listing event.
- REST API can show the previous and updated pricing activity.

---

### 4.3 Vehicle Sold

A producer publishes an event when a vehicle is sold.

Example event type:

```text
LISTING_SOLD
```

Expected outcome:

- Sold event is published to Kafka.
- Consumer persists the sale event.
- REST API can show that the listing moved into a sold state.

---

### 4.4 Malformed Event Handling

If an event is missing required fields, has an invalid event type, or contains invalid data, the consumer should not crash or silently ignore the problem.

Expected outcome:

- Invalid event is rejected by validation logic.
- Invalid event is published to a dead-letter Kafka topic.
- Error reason is logged.
- The main consumer flow continues processing other events.

---

## 5. Stakeholder-Facing Deliverables

The final project should include the following deliverables.

---

## 6. Deliverable 1: Working Spring Boot Producer Service

### Description

A Spring Boot service responsible for publishing vehicle listing events to Kafka.

### Required Capabilities

The producer service should be able to publish events for:

- New listing created.
- Listing price updated.
- Listing sold.

### Expected Endpoints

The producer service should expose simple REST endpoints that allow a user or tester to trigger event publication.

Example endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/listings/events/create` | Publish a new listing event |
| `POST` | `/api/listings/events/price-update` | Publish a price update event |
| `POST` | `/api/listings/events/sold` | Publish a sold listing event |

### Expected Producer Behavior

When an endpoint is called, the producer should:

1. Accept a request body.
2. Validate basic request structure.
3. Build a vehicle listing event object.
4. Serialize the event as JSON.
5. Publish the event to the main Kafka topic.
6. Return a success response showing the event was accepted for publishing.

### Example Producer Response

```json
{
  "status": "PUBLISHED",
  "topic": "vehicle-listing-events",
  "eventType": "PRICE_UPDATED",
  "listingId": "LST-1001",
  "message": "Vehicle listing event published successfully."
}
```

### Stakeholder Value

This deliverable demonstrates that the system can accept listing-related actions and convert them into Kafka events for asynchronous downstream processing.

---

## 7. Deliverable 2: Kafka Topic Design

### Description

Kafka should be used as the central event transport layer between the producer and consumer services.

### Required Kafka Topics

| Topic Name | Purpose |
|---|---|
| `vehicle-listing-events` | Main topic for valid vehicle listing events |
| `vehicle-listing-events-dlq` | Dead-letter queue topic for malformed or unprocessable events |

### Main Topic Responsibility

The main Kafka topic should carry listing lifecycle events such as:

- Listing created.
- Price updated.
- Listing sold.

### Dead-Letter Queue Responsibility

The DLQ topic should capture malformed events that cannot be processed by the consumer.

Examples of malformed events:

- Missing `listingId`.
- Missing `eventType`.
- Invalid event type.
- Negative price.
- Missing vehicle details for a new listing event.
- Invalid timestamp format.
- JSON deserialization failure.

### Stakeholder Value

Kafka topics make the architecture asynchronous, scalable, and extensible. The DLQ adds reliability and observability by ensuring invalid events are not lost.

---

## 8. Deliverable 3: Event Contract

### Description

A clearly defined event contract should describe the structure of messages exchanged through Kafka.

### Vehicle Listing Event Schema

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

### Required Fields

| Field | Type | Required | Description |
|---|---:|---:|---|
| `eventId` | String | Yes | Unique ID for the event |
| `eventType` | String | Yes | Type of listing event |
| `listingId` | String | Yes | Unique listing identifier |
| `vin` | String | Conditional | Vehicle identification number |
| `make` | String | Conditional | Vehicle manufacturer |
| `model` | String | Conditional | Vehicle model |
| `year` | Integer | Conditional | Vehicle year |
| `price` | Decimal | Conditional | Current listing price |
| `previousPrice` | Decimal | No | Previous price before update |
| `status` | String | Yes | Listing status |
| `dealerId` | String | Yes | Dealer or source identifier |
| `eventTimestamp` | String | Yes | Timestamp of the event |

### Supported Event Types

| Event Type | Description |
|---|---|
| `LISTING_CREATED` | A new listing was added |
| `PRICE_UPDATED` | Listing price changed |
| `LISTING_SOLD` | Vehicle was sold |

### Supported Listing Statuses

| Status | Description |
|---|---|
| `ACTIVE` | Listing is available |
| `SOLD` | Vehicle has been sold |
| `UPDATED` | Listing was modified |

### Stakeholder Value

A clear event contract reduces ambiguity between producer and consumer services and makes the system easier to maintain, test, and extend.

---

## 9. Deliverable 4: Working Spring Boot Consumer Service

### Description

A Spring Boot service responsible for consuming vehicle listing events from Kafka, validating them, persisting them to MySQL, and redirecting malformed events to the DLQ.

### Required Capabilities

The consumer service should:

1. Subscribe to `vehicle-listing-events`.
2. Deserialize incoming JSON events.
3. Validate required fields.
4. Persist valid events to MySQL.
5. Send invalid events to `vehicle-listing-events-dlq`.
6. Log successful and failed processing attempts.

### Consumer Processing Flow

```text
Kafka Event Received
        |
        v
Deserialize JSON
        |
        v
Validate Event
        |
        |--- valid ---> Persist to MySQL
        |
        |--- invalid -> Publish to DLQ + Log Error
```

### Error Handling Requirements

The consumer should gracefully handle:

- Invalid JSON.
- Missing required fields.
- Unsupported event types.
- Database save failures.
- Kafka publish failures to DLQ.

### Stakeholder Value

This deliverable demonstrates the core event-driven backend behavior. It proves that the system can process asynchronous events reliably and persist meaningful listing history.

---

## 10. Deliverable 5: MySQL Persistence Layer

### Description

The consumer service should persist listing event history to a MySQL database using Hibernate/JPA.

### Database Name

```text
vehicle_listing_db
```

### Suggested Table: `listing_history`

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT | Primary key |
| `event_id` | VARCHAR(100) | Unique event identifier |
| `event_type` | VARCHAR(50) | Listing event type |
| `listing_id` | VARCHAR(100) | Listing identifier |
| `vin` | VARCHAR(50) | Vehicle identification number |
| `make` | VARCHAR(100) | Vehicle make |
| `model` | VARCHAR(100) | Vehicle model |
| `vehicle_year` | INT | Vehicle year |
| `price` | DECIMAL(12,2) | Current price |
| `previous_price` | DECIMAL(12,2) | Previous price |
| `status` | VARCHAR(50) | Listing status |
| `dealer_id` | VARCHAR(100) | Dealer identifier |
| `event_timestamp` | DATETIME | Original event timestamp |
| `processed_at` | DATETIME | Time consumer processed event |

### Persistence Requirements

The application should use:

- Spring Data JPA repositories.
- Hibernate entity mapping.
- MySQL as the database.
- Automatic schema generation for local development, or SQL migration scripts if preferred.
- Proper indexing on frequently queried fields.

### Recommended Indexes

| Index | Purpose |
|---|---|
| `listing_id` | Query all history for one listing |
| `event_type` | Filter events by event type |
| `dealer_id` | Query events by dealer |
| `event_timestamp` | Sort and filter by event time |

### Stakeholder Value

The persistence layer turns transient Kafka events into durable business history that can be queried and audited.

---

## 11. Deliverable 6: REST API for Listing History

### Description

The consumer service should expose REST APIs to query persisted listing event history.

### Required API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/listings/history` | Get all listing history records |
| `GET` | `/api/listings/{listingId}/history` | Get history for a specific listing |
| `GET` | `/api/listings/history/event-type/{eventType}` | Get history by event type |
| `GET` | `/api/listings/history/dealer/{dealerId}` | Get history by dealer |
| `GET` | `/api/listings/history/recent` | Get recently processed events |

### Example API Response

```json
[
  {
    "eventId": "evt-12345",
    "eventType": "PRICE_UPDATED",
    "listingId": "LST-1001",
    "vin": "1HGCM82633A004352",
    "make": "Toyota",
    "model": "Camry",
    "year": 2022,
    "price": 23900.00,
    "previousPrice": 24500.00,
    "status": "UPDATED",
    "dealerId": "DLR-2001",
    "eventTimestamp": "2026-04-30T14:25:00Z",
    "processedAt": "2026-04-30T14:25:04Z"
  }
]
```

### API Expectations

The API should:

- Return JSON responses.
- Return meaningful HTTP status codes.
- Support simple filtering.
- Avoid exposing internal database entity details directly if DTOs are used.
- Be testable using Postman, curl, or Swagger/OpenAPI.

### Stakeholder Value

The REST API makes the event history accessible to external users, frontend applications, reporting tools, or technical reviewers.

---

## 12. Deliverable 7: Dead-Letter Queue Handling

### Description

Malformed events should not crash the consumer or disappear silently. They should be redirected to a dead-letter Kafka topic for later inspection.

### DLQ Topic

```text
vehicle-listing-events-dlq
```

### DLQ Message Structure

```json
{
  "originalPayload": "{ malformed event payload }",
  "errorReason": "Missing required field: listingId",
  "sourceTopic": "vehicle-listing-events",
  "failedAt": "2026-04-30T14:30:00Z"
}
```

### DLQ Handling Requirements

The system should:

- Capture original malformed payload.
- Capture reason for failure.
- Capture source topic.
- Capture failure timestamp.
- Publish the failed message to the DLQ topic.
- Continue processing future events.

### Stakeholder Value

DLQ handling demonstrates production-oriented thinking. It shows that the system is designed not only for happy-path processing but also for operational failure scenarios.

---

## 13. Deliverable 8: JUnit Test for Consumer Logic

### Description

The project should include at least one meaningful JUnit test that verifies the consumer logic.

### Required Test Coverage

The test should verify that:

1. A valid listing event is consumed and saved successfully.
2. An invalid listing event is not saved.
3. An invalid listing event is routed to the DLQ.

### Suggested Test Class

```text
VehicleListingConsumerTest
```

### Example Test Cases

| Test Name | Purpose |
|---|---|
| `shouldPersistValidListingCreatedEvent` | Confirms valid create event is saved |
| `shouldPersistValidPriceUpdatedEvent` | Confirms price update event is saved |
| `shouldSendMalformedEventToDlq` | Confirms invalid event goes to DLQ |
| `shouldRejectEventWithMissingListingId` | Confirms validation catches missing listing ID |
| `shouldRejectNegativePrice` | Confirms invalid price is rejected |

### Recommended Testing Tools

- JUnit 5.
- Mockito.
- Spring Boot Test.
- Optional: Testcontainers for Kafka and MySQL integration testing.

### Minimum Acceptable Test

At minimum, the project should include a unit test where:

- A sample valid event is passed to the consumer.
- The repository save method is called once.
- The DLQ publisher is not called.

And another test where:

- An invalid event is passed to the consumer.
- The repository save method is never called.
- The DLQ publisher is called once.

### Stakeholder Value

The test deliverable proves that the most important backend behavior is verified and reduces the risk of regressions.

---

## 14. Deliverable 9: Local Development Setup

### Description

The project should include clear instructions for running the full system locally.

### Required Local Components

| Component | Purpose |
|---|---|
| Java 17+ | Runtime for Spring Boot |
| Maven or Gradle | Build tool |
| Docker Desktop | Run Kafka and MySQL locally |
| Kafka | Message broker |
| MySQL | Persistent database |
| Postman or curl | API testing |

### Recommended Docker Services

A `docker-compose.yml` file should be included for:

- Kafka.
- Zookeeper or KRaft-based Kafka setup.
- MySQL.
- Optional Kafka UI.

### Environment Variables

The project should document required configuration values.

Example:

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

### Stakeholder Value

A clear setup process makes the project easier to evaluate, run, demo, and maintain.

---

## 15. Deliverable 10: Repository Structure

### Description

The project should be organized clearly so that a reviewer can understand the architecture quickly.

### Suggested Repository Structure

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
├── docker-compose.yml
├── README.md
├── DELIVERABLES.md
└── docs/
    ├── architecture.md
    ├── event-contract.md
    ├── api-examples.md
    └── demo-script.md
```

### Stakeholder Value

A clean repository structure makes the project easier to navigate and gives the impression of professional backend engineering.

---

## 16. Deliverable 11: Architecture Documentation

### Description

The project should include a clear architecture explanation and diagram.

### Architecture Overview

```text
Client / Tester
      |
      v
Producer REST API
      |
      v
Kafka Topic: vehicle-listing-events
      |
      v
Consumer Service
      |
      |------ valid event ------> MySQL listing_history table
      |
      |------ invalid event ----> Kafka DLQ Topic
      |
      v
Consumer REST API
      |
      v
Listing History JSON Response
```

### Architecture Explanation

The producer service represents the system where listing actions originate. Instead of directly writing to the database, it publishes events to Kafka. The consumer service listens to Kafka independently, processes events asynchronously, and saves valid events to MySQL.

This separation gives the system better scalability and flexibility. If additional services are needed in the future, such as analytics, fraud detection, notification alerts, or pricing intelligence, they can subscribe to the same Kafka topic without changing the producer.

### Stakeholder Value

The architecture documentation helps non-developers and technical reviewers understand why Kafka is being used and what business problem the design solves.

---

## 17. Deliverable 12: API Documentation

### Description

The project should include API documentation with sample requests and responses.

### Producer API Example

#### Publish Price Update Event

```http
POST /api/listings/events/price-update
Content-Type: application/json
```

```json
{
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "previousPrice": 24500.00,
  "price": 23900.00,
  "dealerId": "DLR-2001"
}
```

### Consumer API Example

#### Get Listing History

```http
GET /api/listings/LST-1001/history
```

```json
[
  {
    "eventType": "LISTING_CREATED",
    "listingId": "LST-1001",
    "price": 24500.00,
    "status": "ACTIVE"
  },
  {
    "eventType": "PRICE_UPDATED",
    "listingId": "LST-1001",
    "previousPrice": 24500.00,
    "price": 23900.00,
    "status": "UPDATED"
  }
]
```

### Stakeholder Value

API documentation makes the system easy to test and demonstrates that the project is usable, not just technically implemented.

---

## 18. Deliverable 13: Demo Script

### Description

The project should include a short demo script that explains exactly how to present the system.

### Demo Flow

1. Start Kafka and MySQL using Docker Compose.
2. Start the producer service.
3. Start the consumer service.
4. Send a `LISTING_CREATED` event through the producer API.
5. Send a `PRICE_UPDATED` event through the producer API.
6. Send a `LISTING_SOLD` event through the producer API.
7. Query the consumer API to show listing history.
8. Send a malformed event.
9. Show that the malformed event was sent to the DLQ.
10. Run JUnit tests and show passing results.

### Demo Success Criteria

The demo is successful if:

- Kafka starts successfully.
- Producer publishes events successfully.
- Consumer receives events successfully.
- MySQL contains persisted listing history.
- REST API returns listing history.
- Malformed event is routed to DLQ.
- JUnit tests pass.

### Stakeholder Value

The demo script ensures the project can be presented clearly in interviews, portfolio reviews, or stakeholder meetings.

---

## 19. Deliverable 14: Logging and Observability

### Description

The system should include meaningful logs for important processing steps.

### Required Logs

The application should log:

- Event received by producer.
- Event published to Kafka.
- Event consumed from Kafka.
- Event validation success.
- Event validation failure.
- Event persisted to MySQL.
- Event sent to DLQ.
- Unexpected processing errors.

### Example Logs

```text
INFO  Published event LISTING_CREATED for listingId=LST-1001 to topic=vehicle-listing-events
INFO  Consumed event PRICE_UPDATED for listingId=LST-1001
INFO  Persisted eventId=evt-12345 for listingId=LST-1001
WARN  Invalid event received. Reason=Missing listingId. Sending to DLQ.
```

### Stakeholder Value

Good logging makes the project easier to debug and shows awareness of production support needs.

---

## 20. Deliverable 15: Validation Rules

### Description

The consumer should validate incoming events before saving them.

### Required Validation Rules

| Rule | Expected Behavior |
|---|---|
| `eventId` must not be blank | Reject event if missing |
| `eventType` must be supported | Reject unsupported event type |
| `listingId` must not be blank | Reject event if missing |
| `dealerId` must not be blank | Reject event if missing |
| `price` must not be negative | Reject event if negative |
| `eventTimestamp` must be valid | Reject event if invalid |
| `LISTING_CREATED` should include vehicle details | Reject if make/model/year are missing |
| `PRICE_UPDATED` should include price | Reject if new price is missing |
| `LISTING_SOLD` should set status to `SOLD` | Reject or normalize invalid status |

### Stakeholder Value

Validation prevents bad data from entering the database and demonstrates responsible backend design.

---

## 21. Deliverable 16: Configuration Management

### Description

Configuration should be externalized instead of hardcoded.

### Required Configurations

The application should allow configuration of:

- Kafka bootstrap servers.
- Kafka topic names.
- Consumer group ID.
- MySQL database URL.
- MySQL username.
- MySQL password.
- Server port.
- Hibernate settings.

### Example Spring Configuration

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

### Stakeholder Value

Externalized configuration makes the project easier to deploy in different environments without changing the source code.

---

## 22. Deliverable 17: README Documentation

### Description

The project should include a strong README file that explains what the project does and how to run it.

### README Should Include

- Project overview.
- Why Kafka is used.
- Architecture diagram.
- Technologies used.
- Local setup instructions.
- How to start Kafka and MySQL.
- How to run producer service.
- How to run consumer service.
- API examples.
- Database schema.
- DLQ behavior.
- Testing instructions.
- Demo script.
- Future improvements.

### Stakeholder Value

A strong README makes the project self-explanatory and portfolio-ready.

---

## 23. Recommended Technology Stack

| Area | Technology |
|---|---|
| Backend Framework | Spring Boot |
| Language | Java |
| Messaging | Apache Kafka |
| Database | MySQL |
| ORM | Hibernate/JPA |
| Testing | JUnit 5, Mockito |
| Build Tool | Maven |
| Local Infrastructure | Docker Compose |
| API Testing | Postman or curl |
| Optional Docs | Swagger/OpenAPI |

---

## 24. Non-Functional Requirements

### Reliability

The consumer should not crash permanently because of one malformed event.

### Scalability

Kafka allows the consumer service to scale independently from the producer service.

### Maintainability

Code should be organized into controllers, services, event models, repositories, validators, and configuration classes.

### Testability

Consumer logic should be testable without requiring the full application to run.

### Observability

Logs should make it possible to trace event flow from producer to consumer to database or DLQ.

### Portability

The system should run locally using Docker Compose and environment-based configuration.

---

## 25. Acceptance Criteria

The project is considered complete when the following criteria are met.

### Producer Acceptance Criteria

- Producer service starts successfully.
- Producer exposes REST endpoints to publish listing events.
- Producer publishes valid JSON events to Kafka.
- Producer returns clear success or error responses.

### Kafka Acceptance Criteria

- Main listing topic exists.
- DLQ topic exists.
- Events are visible in the main Kafka topic.
- Malformed events are visible in the DLQ topic.

### Consumer Acceptance Criteria

- Consumer service starts successfully.
- Consumer subscribes to the main Kafka topic.
- Consumer validates incoming events.
- Consumer persists valid events to MySQL.
- Consumer redirects malformed events to DLQ.
- Consumer logs event processing activity.

### Database Acceptance Criteria

- MySQL database starts locally.
- Listing history table is created.
- Valid events are saved correctly.
- Listing history can be queried by listing ID.

### REST API Acceptance Criteria

- Listing history API returns saved events.
- API supports querying by listing ID.
- API supports querying recent history.
- API returns meaningful status codes.

### Testing Acceptance Criteria

- JUnit tests exist for consumer logic.
- Valid event test passes.
- Invalid event or DLQ test passes.
- Tests can be run using Maven or Gradle.

### Documentation Acceptance Criteria

- README explains setup and usage.
- Event contract is documented.
- API examples are provided.
- Demo script is included.

---

## 26. Suggested Milestones

### Milestone 1: Project Setup

Deliverables:

- Producer Spring Boot project created.
- Consumer Spring Boot project created.
- Docker Compose file created.
- Kafka and MySQL running locally.

### Milestone 2: Kafka Producer

Deliverables:

- Producer event model created.
- Producer REST endpoints implemented.
- Kafka producer configuration completed.
- Events published to Kafka successfully.

### Milestone 3: Kafka Consumer and Persistence

Deliverables:

- Consumer Kafka listener implemented.
- Event validation added.
- JPA entity and repository created.
- Valid events saved to MySQL.

### Milestone 4: REST API

Deliverables:

- Listing history API implemented.
- Query by listing ID implemented.
- Query by event type or dealer implemented.
- API tested with sample data.

### Milestone 5: DLQ and Testing

Deliverables:

- DLQ publisher implemented.
- Malformed event handling completed.
- JUnit tests added.
- README and demo script finalized.

---

## 27. Suggested Future Enhancements

These are not required for the initial version but can make the project stronger.

### 27.1 Add Swagger/OpenAPI

Expose interactive API documentation for producer and consumer endpoints.

### 27.2 Add Testcontainers

Use Testcontainers to run Kafka and MySQL during integration tests.

### 27.3 Add Idempotency

Prevent duplicate event processing by enforcing uniqueness on `eventId`.

### 27.4 Add Event Versioning

Add a field such as:

```json
{
  "schemaVersion": "1.0"
}
```

This makes the event contract easier to evolve.

### 27.5 Add Kafka UI

Add Kafka UI to Docker Compose so topics and messages can be inspected visually.

### 27.6 Add Metrics

Expose metrics such as:

- Number of events consumed.
- Number of events persisted.
- Number of DLQ events.
- Consumer processing latency.

### 27.7 Add Frontend Dashboard

Build a simple React dashboard to show listing history and event status.

---

## 28. Risks and Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| Kafka setup complexity | Local setup may fail | Use Docker Compose and clear README |
| Malformed messages crash consumer | Event pipeline becomes unreliable | Add validation and DLQ handling |
| Duplicate event processing | Database may contain duplicates | Add unique constraint on `eventId` |
| Weak documentation | Stakeholder cannot evaluate project easily | Include README, event contract, and demo script |
| Tests too shallow | Project appears incomplete | Add focused JUnit tests for consumer logic |

---

## 29. Definition of Done

The project is done when a reviewer can:

1. Clone the repository.
2. Run Kafka and MySQL locally.
3. Start the producer and consumer services.
4. Publish vehicle listing events through REST endpoints.
5. Confirm events are consumed from Kafka.
6. Confirm valid events are saved to MySQL.
7. Query listing history through REST APIs.
8. Send a malformed event and see it routed to the DLQ.
9. Run JUnit tests successfully.
10. Understand the architecture and usage from the README.

---

## 30. Final Stakeholder Summary

This project will deliver a complete event-driven backend system for vehicle listing history. It is intentionally designed to be simple enough to run locally but realistic enough to demonstrate production-relevant backend engineering skills.

The most important value of the project is that it shows more than CRUD development. It demonstrates asynchronous messaging, service separation, Kafka event processing, persistence, failure handling through a dead-letter queue, REST API design, and automated testing.

For a technical reviewer or hiring stakeholder, this project clearly communicates that the developer understands modern backend architecture and can build systems that are scalable, testable, and resilient.
