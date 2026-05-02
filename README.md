# Vehicle Listing Event Pipeline

Spring Boot and Kafka project for publishing, consuming, persisting, and viewing vehicle listing events.

## Local Environment

Copy the example environment file before starting local services.

PowerShell:

```powershell
Copy-Item .env.example .env
```

Git Bash, macOS, or Linux:

```bash
cp .env.example .env
```

`.env` is for local machine settings only and is ignored by Git.

## Start Phase 1 Infrastructure

This project uses Docker for MySQL, Kafka, Zookeeper, and Kafka UI. You do not need to install MySQL locally.

```bash
docker compose up -d
```

Kafka UI:

```text
http://localhost:8080
```

Expected Kafka topics:

- `vehicle-listing-events`
- `vehicle-listing-events-dlq`

MySQL:

- Host: `localhost`
- Port: `3306`
- Database: `vehicle_listing_db`
- User: `root`
- Password: `password`
