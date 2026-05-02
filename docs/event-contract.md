# Vehicle Listing Event Contract

This document is the source of truth for messages exchanged through Kafka in the vehicle listing event pipeline. The producer must publish events that match this contract, and the consumer must validate incoming events against this contract before saving them to MySQL.

## Kafka Topics

| Topic | Purpose |
|---|---|
| `vehicle-listing-events` | Main topic for valid vehicle listing lifecycle events. |
| `vehicle-listing-events-dlq` | Dead-letter queue topic for malformed, invalid, or unprocessable events. |

Events are serialized as JSON. Timestamps must use ISO-8601 UTC format, for example `2026-04-30T14:25:00Z`. Money values must be JSON numbers and should be handled as decimal values in application code.

## VehicleListingEvent Schema

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

## Fields

| Field | JSON Type | Required | Description |
|---|---|---|---|
| `eventId` | string | Yes | Unique identifier for this event. |
| `eventType` | string | Yes | Type of listing event. Must be a supported event type. |
| `listingId` | string | Yes | Unique identifier for the listing. |
| `vin` | string | Conditional | Vehicle identification number. Required for `LISTING_CREATED`. |
| `make` | string | Conditional | Vehicle manufacturer. Required for `LISTING_CREATED`. |
| `model` | string | Conditional | Vehicle model. Required for `LISTING_CREATED`. |
| `year` | number | Conditional | Vehicle year. Required for `LISTING_CREATED`. |
| `price` | number | Conditional | Current listing price. Required for `LISTING_CREATED` and `PRICE_UPDATED`. |
| `previousPrice` | number or null | No | Previous listing price before a price update. |
| `status` | string | Yes | Listing status after the event is applied. |
| `dealerId` | string | Yes | Dealer or source identifier. |
| `eventTimestamp` | string | Yes | ISO-8601 UTC timestamp for when the event occurred. |

## Supported Event Types

| Event Type | Description | Expected Status |
|---|---|---|
| `LISTING_CREATED` | A new vehicle listing was added. | `ACTIVE` |
| `PRICE_UPDATED` | The listing price changed. | `UPDATED` |
| `LISTING_SOLD` | The vehicle was sold. | `SOLD` |

Unsupported event types must be rejected by the consumer and routed to the DLQ.

## Supported Status Values

| Status | Description |
|---|---|
| `ACTIVE` | Listing is available. |
| `UPDATED` | Listing was modified. |
| `SOLD` | Vehicle has been sold. |

## Validation Rules

The consumer must validate events before saving them to MySQL.

| Rule | Expected Behavior |
|---|---|
| `eventId` must not be blank. | Reject event and route to DLQ. |
| `eventType` must not be blank. | Reject event and route to DLQ. |
| `eventType` must be one of `LISTING_CREATED`, `PRICE_UPDATED`, or `LISTING_SOLD`. | Reject event and route to DLQ. |
| `listingId` must not be blank. | Reject event and route to DLQ. |
| `dealerId` must not be blank. | Reject event and route to DLQ. |
| `status` must not be blank. | Reject event and route to DLQ. |
| `eventTimestamp` must not be blank. | Reject event and route to DLQ. |
| `eventTimestamp` must parse as a valid ISO-8601 timestamp. | Reject event and route to DLQ. |
| `price` must not be negative when present. | Reject event and route to DLQ. |
| `previousPrice` must not be negative when present. | Reject event and route to DLQ. |
| `LISTING_CREATED` must include `vin`, `make`, `model`, `year`, and `price`. | Reject event and route to DLQ. |
| `PRICE_UPDATED` must include `price`. | Reject event and route to DLQ. |
| `LISTING_SOLD` must have status `SOLD`. | Reject event and route to DLQ. |

## Valid Event Examples

### LISTING_CREATED

```json
{
  "eventId": "evt-1001-created",
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

### PRICE_UPDATED

```json
{
  "eventId": "evt-1001-price-updated",
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
  "eventTimestamp": "2026-04-30T15:10:00Z"
}
```

### LISTING_SOLD

```json
{
  "eventId": "evt-1001-sold",
  "eventType": "LISTING_SOLD",
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": 23900.00,
  "previousPrice": null,
  "status": "SOLD",
  "dealerId": "DLR-2001",
  "eventTimestamp": "2026-04-30T16:45:00Z"
}
```

## Malformed Event Examples

### Missing listingId

```json
{
  "eventId": "evt-invalid-missing-listing",
  "eventType": "LISTING_CREATED",
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

Expected error reason:

```text
Missing required field: listingId
```

### Negative price

```json
{
  "eventId": "evt-invalid-negative-price",
  "eventType": "PRICE_UPDATED",
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": -100.00,
  "previousPrice": 24500.00,
  "status": "UPDATED",
  "dealerId": "DLR-2001",
  "eventTimestamp": "2026-04-30T15:10:00Z"
}
```

Expected error reason:

```text
Invalid field: price must not be negative
```

## DLQ Message Structure

Invalid events must be published to `vehicle-listing-events-dlq` using this structure:

```json
{
  "originalPayload": "{ malformed event payload }",
  "errorReason": "Missing required field: listingId",
  "sourceTopic": "vehicle-listing-events",
  "failedAt": "2026-04-30T14:30:00Z"
}
```

| Field | JSON Type | Required | Description |
|---|---|---|---|
| `originalPayload` | string | Yes | Original raw event payload when available. |
| `errorReason` | string | Yes | Human-readable reason the event failed processing. |
| `sourceTopic` | string | Yes | Kafka topic where the failed event was received. |
| `failedAt` | string | Yes | ISO-8601 UTC timestamp for when failure handling occurred. |

The consumer must continue processing future events after publishing a DLQ message.
