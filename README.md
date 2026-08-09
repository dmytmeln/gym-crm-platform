# Gym CRM Platform

![Build](https://github.com/dmytmeln/gym-crm-platform/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=coverage)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=alert_status)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)

## Platform Services

* **[Discovery Server](discovery-server/README.md)**: Service registry (Eureka Server) that enables other microservices to locate and communicate with each other.
* **[API Gateway](api-gateway/README.md)**: Gateway service that routes requests to appropriate microservices.
* **[Workload Service](workload-service/README.md)**: A service for managing and tracking trainer workload.
* **[Gym Core Service](gym-core-service/README.md)**: The core CRM backend dealing with trainees, trainers, training sessions, and user authentication.

## Run the Full Platform with Docker Compose

The repository ships container images and a full-stack `compose.yaml` that bring up **all four services plus MySQL, Redis, MongoDB, and ActiveMQ** with a single command.

### Prerequisites

* Docker with the Compose v2 plugin (`docker compose version`)

### Start the Stack

```bash
docker compose up --wait
```

Exposed services and web consoles:

| Endpoint             | URL                                            | Credentials   |
|----------------------|------------------------------------------------|---------------|
| API Gateway          | [http://localhost:8080](http://localhost:8080) | —             |
| Eureka Dashboard     | [http://localhost:8761](http://localhost:8761) | —             |
| ActiveMQ Web Console | [http://localhost:8161](http://localhost:8161) | `gym` / `gym` |

All other ports (MySQL 3306, Redis 6379, MongoDB 27017, OpenWire 61616, service ports 8081/8082) stay **internal to the compose network** and are never published to the host.

### Configuration

All configuration and credentials are injected through environment variables. Dev defaults live in `compose.yaml` (every placeholder has a `${VAR:-default}`), so the stack boots even without any configuration file. To override anything:

```bash
cp .env.example .env   # then edit .env
```

### Managing the Stack

```bash
docker compose ps                # status + health of every container
docker compose logs -f <service> # tail logs (service names match compose.yaml)
docker compose down              # stop the stack (data survives)
docker compose down -v           # stop and delete the named volumes (MySQL/Mongo/ActiveMQ data)
```

MySQL, MongoDB, and ActiveMQ data lives in named volumes (`mysql-data`, `mongo-data`, `activemq-data`).

### Building an Individual Image

Each service has its own multi-stage Dockerfile, so any service can be built independently from the repository root:

```bash
docker build -f discovery-server/Dockerfile -t discovery-server:1.0-SNAPSHOT .
docker build -f api-gateway/Dockerfile -t api-gateway:1.0-SNAPSHOT .
docker build -f gym-core-service/Dockerfile -t gym-core-service:1.0-SNAPSHOT .
docker build -f workload-service/Dockerfile -t workload-service:1.0-SNAPSHOT .
```

## Prerequisites

* Git (2.40+)
* JDK 21
* Apache Maven (3.8+)
* Docker with Compose v2 (required for the full-stack compose workflow and for running integration tests via Testcontainers)

## Quick Start Guide

### Step 1: Clone the Project

```bash
git clone https://github.com/dmytmeln/gym-crm-platform.git
cd gym-crm-platform
```

### Step 2: Build the Platform

Build all services from the root directory:

```bash
mvn clean compile
```

To build and run all tests for all modules (requires Docker to be running):

```bash
mvn clean install
```

> [!TIP]
> The recommended way to run the platform is the [Docker Compose workflow](#run-the-full-platform-with-docker-compose) above — one command brings up every service and all shared infrastructure.

### Step 3: Start Shared Infrastructure

For local development, both `gym-core-service` and `workload-service` require an ActiveMQ broker.

Local profiles in both services default to:

* **Broker URL**: `failover:(tcp://localhost:61616)`
* **Username**: `gym`
* **Password**: `gym`

The Apache ActiveMQ Classic image can be run standalone as:

```bash
docker run --name gym-activemq -p 61616:61616 -p 8161:8161 -d apache/activemq:6.3.0
```

Optional web console: [http://localhost:8161](http://localhost:8161) (default login `admin` / `admin` for a standalone run; the compose stack uses `gym` / `gym`)

If your broker credentials differ, override via `SPRING_ACTIVEMQ_USER` / `SPRING_ACTIVEMQ_PASSWORD` environment variables.

### Step 4: Run the Services

For a fully functional platform, the services should be started in the following order:

1. **[Discovery Server Setup & Run](discovery-server/README.md)** (Eureka Server, port `8761`)
2. **[API Gateway Setup & Run](api-gateway/README.md)** (Port `8080`)
3. **[Workload Service Setup & Run](workload-service/README.md)** (Port `8081`)
4. **[Gym Core Service Setup & Run](gym-core-service/README.md)** (Port `8082`)

Please refer to individual service documentation for service-specific setup and configuration requirements.

---

## Postman Collection

The project includes a Postman collection and environment for testing the API. You can find them at the following relative paths:
* **Postman Collection**: [docs/postman/GIA API.postman_collection.json](docs/postman/GIA%20API.postman_collection.json)
* **Postman Environment**: [docs/postman/GIA API Dev Environment.postman_environment.json](docs/postman/GIA%20API%20Dev%20Environment.postman_environment.json)

## Running Tests

To run all unit and integration tests across all modules (requires Docker to be running):

```bash
mvn test
```

To run tests for a specific module:

```bash
mvn test -pl gym-core-service
```
