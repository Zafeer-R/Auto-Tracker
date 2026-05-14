# API Examples

These examples assume the local Docker infrastructure is running, the producer service is on `http://localhost:8081`, and the consumer service is on `http://localhost:8082`.

## Producer API

### Publish Listing Created

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/listings/events/create" -ContentType "application/json" -Body '{
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": 24500.00,
  "dealerId": "DLR-2001"
}'
```

### Publish Price Updated

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/listings/events/price-update" -ContentType "application/json" -Body '{
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "previousPrice": 24500.00,
  "price": 23900.00,
  "dealerId": "DLR-2001"
}'
```

### Publish Listing Sold

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/listings/events/sold" -ContentType "application/json" -Body '{
  "listingId": "LST-1001",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": 23900.00,
  "dealerId": "DLR-2001"
}'
```

## Consumer Listing History API

### Get All History

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/listings/history"
```

### Get History For One Listing

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/listings/LST-1001/history"
```

### Get History By Event Type

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/listings/history/event-type/PRICE_UPDATED"
```

Supported event type filters are `LISTING_CREATED`, `PRICE_UPDATED`, and `LISTING_SOLD`.

### Get History By Dealer

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/listings/history/dealer/DLR-2001"
```

### Get Recent History

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/api/listings/history/recent"
```

## Sample History Response

```json
[
  {
    "eventId": "68ff8abb-c7bc-4980-8711-b870c41678ef",
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
    "eventTimestamp": "2026-04-30T14:25:00Z",
    "processedAt": "2026-04-30T14:25:01Z"
  }
]
```

Empty matches return an empty JSON array:

```json
[]
```

## Malformed Kafka Events For DLQ Testing

These payloads should be sent directly to Kafka topic `vehicle-listing-events`, not through the producer API. The producer validates normal REST input, so direct Kafka publishing is the easiest way to demo malformed-event handling.

### Missing Listing ID

```json
{
  "eventId": "evt-invalid-missing-listing",
  "eventType": "LISTING_CREATED",
  "vin": "1HGCM82633A004352",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "price": 24500.00,
  "status": "ACTIVE",
  "dealerId": "DLR-2001",
  "eventTimestamp": "2026-04-30T14:25:00Z"
}
```

### Unsupported Event Type

```json
{
  "eventId": "evt-invalid-type",
  "eventType": "LISTING_RELISTED",
  "listingId": "LST-1001",
  "dealerId": "DLR-2001",
  "status": "ACTIVE",
  "eventTimestamp": "2026-04-30T14:25:00Z"
}
```

### Negative Price

```json
{
  "eventId": "evt-invalid-negative-price",
  "eventType": "PRICE_UPDATED",
  "listingId": "LST-1001",
  "price": -100.00,
  "status": "UPDATED",
  "dealerId": "DLR-2001",
  "eventTimestamp": "2026-04-30T14:25:00Z"
}
```

### Invalid Timestamp

```json
{
  "eventId": "evt-invalid-timestamp",
  "eventType": "LISTING_SOLD",
  "listingId": "LST-1001",
  "price": 23900.00,
  "status": "SOLD",
  "dealerId": "DLR-2001",
  "eventTimestamp": "not-a-timestamp"
}
```

### Invalid JSON

```text
{ invalid-json
```

Expected DLQ message shape:

```json
{
  "originalPayload": "{ malformed event payload }",
  "errorReason": "Missing required field: listingId",
  "sourceTopic": "vehicle-listing-events",
  "failedAt": "2026-04-30T14:30:00Z"
}
```
