# Gym CRM Platform

![Build](https://github.com/dmytmeln/gym-crm-platform/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=coverage)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm-platform&metric=alert_status)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm-platform)

## Platform Services

* **[Gym Core Service](gym-core-service/README.md)**: The core CRM backend dealing with trainees, trainers, training sessions, and user authentication.
* **Workload Service**: A service for managing and tracking trainer workload.

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

Please refer to individual service documentation for running each service:
* **[Gym Core Service Setup & Run](gym-core-service/README.md)**

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
