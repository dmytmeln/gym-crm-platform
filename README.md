# Gym CRM Platform

![Build](https://github.com/dmytmeln/gym-crm-platform/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=coverage)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=alert_status)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)

## Platform Services

* **[Discovery Server](discovery-server/README.md)**: Service registry (Eureka Server) that enables other microservices to locate and communicate with each other.
* **[Workload Service](workload-service/README.md)**: A service for managing and tracking trainer workload.
* **[Gym Core Service](gym-core-service/README.md)**: The core CRM backend dealing with trainees, trainers, training sessions, and user authentication.

## Prerequisites

* Git (2.40+)
* JDK 21
* Apache Maven (3.8+)
* Docker (Required for running integration tests via Testcontainers, or optionally for running MySQL and Redis)

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

### Step 3: Run the Services

For a fully functional platform, the services should be started in the following order:

1. **[Discovery Server Setup & Run](discovery-server/README.md)** (Eureka Server, port `8761`)
2. **[Workload Service Setup & Run](workload-service/README.md)** (Port `8081`)
3. **[Gym Core Service Setup & Run](gym-core-service/README.md)** (Port `8080`)

Please refer to individual service documentation for specific setup and configuration requirements.

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
