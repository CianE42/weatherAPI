# Weather Metrics API

A small REST API that ingests weather sensor readings and supports queries (min/max/sum/avg) over a date range, optionally filtered by sensor(s) and metric(s).

- **Language:** Java 21 (Spring Boot 3)
- **DB:** MongoDB (local or Docker)
- **Tests:** JUnit 5, Mockito (unit), Testcontainers + MockMvc (integration)
- **Endpoints:**

  - `POST /sensors/data` — ingest a reading
  - `GET  /sensors/query` — aggregate stats (min/max/sum/avg)

## Quick start

### 0 Prereqs

- Java 21 (Temurin recommended)
- Maven 3.9+

- **Option A (Recommended):** Docker Desktop (for both running MongoDB _and_ running integration tests with Testcontainers)
  OR
- **Option B:** Local MongoDB 6/7

> If you have Docker Desktop, you don’t need MongoDB installed locally.

### 1 Clone and build

```bash

git clone https://github.com/CianE42/weatherAPI.git

cd weatherAPI

mvn clean package

```

### 2 Choose how to run MongoDB

#### Option A — Docker Compose (repo file)

```bash
docker compose up -d

```

> exposes Mongo on localhost:27017

#### Option C — Local MongoDB service

Install and start MongoDB 6/7; ensure it listens on `mongodb://localhost:27017`.

### 3 Configure the app (optional)

By default the app uses:

```
spring.data.mongodb.uri=mongodb://localhost:27017/weatherdb
```

Override via env var `MONGO_URI`:

```bash
export MONGO_URI="mongodb://localhost:27017/weatherdb"
```

### 4 Run the app

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

---

# Demo Data Seeder

When running with the demo profile, the application will auto-populate MongoDB with synthetic sensor data:

3 sensors (1, 2, 3)
3 metrics (temperature, humidity, wind_speed)
Data points every 6 hours for the last 7 days

Collection is dropped and reseeded fresh on every startup

This makes it easy to test the API without having to manually insert data.
Run with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## API

### Ingest reading

```
POST /sensors/data
Content-Type: application/json
```

Body:

```json
{
  "sensorId": "1",
  "metric": "temperature",
  "value": 22.5,
  "timestamp": "2025-08-16T12:00:00Z"
}
```

Notes:

- `metric` is validated and normalized; accepted (case/format-insensitive):
  `temperature`, `humidity`, `wind_speed` (also accepts `wind-speed`, `windSpeed`).
- `timestamp` must be ISO-8601 UTC (e.g., `...Z`).

**curl**

```bash
curl -X POST http://localhost:8080/sensors/data \
  -H 'Content-Type: application/json' \
  -d '{
    "sensorId":"1","metric":"Wind-Speed","value":10.0,"timestamp":"2025-08-16T12:00:00Z"
  }'
```

### Query aggregates

```
GET /sensors/query
```

Query params:

- `sensorIds` — optional comma list; omit = all sensors
- `metrics` — optional comma list; omit = all metrics (validated/normalized)
- `stat` — `min|max|sum|avg` (default `avg`)
- `from`, `to` — ISO-8601 instants; if omitted, defaults to the last 24h
  (When supplied, must be between **1 and 31 days**.)

**Examples**

Average temperature & humidity for sensor 1 between Aug 1–3:

```bash
curl "http://localhost:8080/sensors/query?sensorIds=1&metrics=temperature,humidity&stat=avg&from=2025-08-01T00:00:00Z&to=2025-08-03T23:59:59Z"
```

Max temperature for sensor 1 in the same window:

```bash
curl "http://localhost:8080/sensors/query?sensorIds=1&metrics=temperature&stat=max&from=2025-08-01T00:00:00Z&to=2025-08-03T23:59:59Z"
```

Using defaults (last 24h, all sensors/metrics, avg):

```bash
curl "http://localhost:8080/sensors/query"
```

Sample response:

```json
{
  "sensorIds": ["1"],
  "metrics": ["temperature", "humidity"],
  "statistic": "avg",
  "from": "2025-08-01T00:00:00Z",
  "to": "2025-08-03T23:59:59Z",
  "resultsByMetric": {
    "temperature": 22.0,
    "humidity": 60.0
  }
}
```

---

## Running tests

### Unit tests (fast)

```bash
mvn -Dtest="*ServiceTest,*ModelTest" test
```

### Integration tests (use Testcontainers MongoDB)

**Docker Desktop must be running**.

```bash
mvn test
```

> If you can’t run Docker, you can skip integration tests by running only the unit tests above.

---

## Postman collection

Import the provided Postman collection from `postman/WeatherMetrics.postman_collection.json` (if included), or create two requests:

- `POST /sensors/data` with example body
- `GET /sensors/query` with params shown above

---

**Performance:** a compound index on `(sensorId, metric, timestamp)` supports fast range scans. Aggregations (min/max/sum/avg) run in Mongo via the aggregation pipeline.

---

## Configuration & environment

- **Port:** 8080 (override via `server.port`)
- **Mongo URI:** `spring.data.mongodb.uri` (env var `MONGO_URI` supported)
- **Time:** timestamps are UTC (`Instant`), pass ISO-8601 (e.g., `2025-08-01T00:00:00Z`)

**macOS / Apple Silicon:** The project uses `mongo:7`, which supports arm64. If you see image issues, pin to `mongo:7.0` explicitly.

**Windows:** Use PowerShell equivalents and ensure Docker Desktop resources are sufficient (CPU/RAM).

---

## Troubleshooting

- **`Failed to load ApplicationContext` in tests**
  Ensure Docker Desktop is running; Testcontainers can’t start Mongo otherwise.

- **`Cannot connect to Mongo` at app start**
  Is Mongo running?

  - Docker: `docker ps` should show a `mongo:7` container.
  - Local: `mongosh "mongodb://localhost:27017/weatherdb"` should connect.

- **Empty query results**
  Check your date window vs stored timestamps. Use `from`/`to` explicitly when testing.

- **Bad Request (400) on metric**
  The API validates metrics. Allowed: `temperature`, `humidity`, `wind_speed` (case/format-insensitive).

---

## What’s implemented

- `POST /sensors/data` with DTO validation and metric normalization
- `GET /sensors/query` with filters (sensorIds, metrics), stats (min/max/sum/avg), date window & sensible defaults
- Mongo aggregation pipeline + compound index
- Global error handling (400s with useful messages)
- Unit tests (service, enum parsing) & integration tests (MockMvc + Testcontainers)
