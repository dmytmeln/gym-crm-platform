# Gym CRM

![Build](https://github.com/dmytmeln/gym-crm/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm&metric=coverage)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=dmytmeln_gym-crm&metric=alert_status)](https://sonarcloud.io/summary/overall?id=dmytmeln_gym-crm)

## Prerequisites

* Git (2.40+)
* JDK 21
* Apache Maven (3.8+)
* MySQL Server (8.0+) or Docker (to run MySQL inside a container)
* Redis Server (6.0+) or Docker (to run Redis inside a container)
* Docker (Required for running integration tests via Testcontainers, or optionally for running MySQL and Redis)

## Quick Start Guide

### Step 1: Clone the Project

```bash
git clone https://github.com/dmytmeln/gym-crm.git
cd gym-crm
```

### Step 2: Set Up the Database

You can either use a locally installed MySQL server or run it using Docker.

#### Option A: Local MySQL Server
```sql
CREATE DATABASE IF NOT EXISTS gym_db;
CREATE USER IF NOT EXISTS 'gym'@'localhost' IDENTIFIED BY 'gym';
GRANT ALL PRIVILEGES ON gym_db.* TO 'gym'@'localhost';
FLUSH PRIVILEGES;
```

#### Option B: Using Docker
If you prefer Docker, you can start a MySQL container with the required database and user setup using this command:

```bash
docker run --name gym-mysql -p 3306:3306 -e MYSQL_DATABASE=gym_db -e MYSQL_USER=gym -e MYSQL_PASSWORD=gym -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0
```

> Database tables and initial schemas will be generated automatically by Liquibase on application startup.

### Step 3: Set Up Redis

You can either use a locally installed Redis server or run it using Docker.

#### Option A: Local Redis Server
Start your local Redis service on the default port (`6379`).

#### Option B: Using Docker
If you prefer Docker, you can start a Redis container with the default port setup using this command:

```bash
docker run --name gym-redis -p 6379:6379 -d redis
```

### Step 4: Build the Application

```bash
mvn clean compile
```

To build a packaged executable JAR and run all tests (requires Docker to be running):

```bash
mvn clean install
```

### Step 5: Run the Application

```bash
mvn exec:java -Dexec.mainClass=com.gym.crm.GymCrmApplication
```

Or using the Spring Boot plugin:

```bash
mvn spring-boot:run
```

The application runs on port 8080 under the context path `/gym-crm`.

* **Base API Path**: [http://localhost:8080/gym-crm/api/v1](http://localhost:8080/gym-crm/api/v1)
* **OpenAPI / Swagger UI**: [http://localhost:8080/gym-crm/swagger-ui/index.html](http://localhost:8080/gym-crm/swagger-ui/index.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8080/gym-crm/v3/api-docs](http://localhost:8080/gym-crm/v3/api-docs)
* **OpenAPI Spec (YAML)**: [src/main/resources/gia-api.yml](gym-core-service/src/main/resources/gia-api.yml) (can be loaded/edited in [Swagger Editor](https://editor.swagger.io/))

## Postman Collection

The project includes a Postman collection and environment for testing the API. You can find them at the following relative paths:
* **Postman Collection**: [docs/postman/GIA API.postman_collection.json](docs/postman/GIA%20API.postman_collection.json)
* **Postman Environment**: [docs/postman/GIA API Dev Environment.postman_environment.json](docs/postman/GIA%20API%20Dev%20Environment.postman_environment.json)

## Running Tests

To run all unit and integration tests (requires Docker to be running):

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=GymCrmApplicationTest
```

## Actuators and Metrics

Spring Boot Actuator and Micrometer are configured to expose system and custom metrics.

### Endpoints

* **Base Actuator Path**: [http://localhost:8080/gym-crm/actuator](http://localhost:8080/gym-crm/actuator)

#### Health Indicators
* **Main Health Status**: [http://localhost:8080/gym-crm/actuator/health](http://localhost:8080/gym-crm/actuator/health)
* **Database Health**: [http://localhost:8080/gym-crm/actuator/health/db](http://localhost:8080/gym-crm/actuator/health/db)
* **Disk Write Health (Custom)**: [http://localhost:8080/gym-crm/actuator/health/diskWrite](http://localhost:8080/gym-crm/actuator/health/diskWrite)
* **Hikari Connection Pool Saturation (Custom)**: [http://localhost:8080/gym-crm/actuator/health/hikariPoolSaturation](http://localhost:8080/gym-crm/actuator/health/hikariPoolSaturation)

#### Metrics
* **Prometheus Metrics**: [http://localhost:8080/gym-crm/actuator/prometheus](http://localhost:8080/gym-crm/actuator/prometheus)
* **Metrics Index**: [http://localhost:8080/gym-crm/actuator/metrics](http://localhost:8080/gym-crm/actuator/metrics)

### Custom Metrics API

* **User Registrations (`gym_crm_user_registrations_total`)**: [http://localhost:8080/gym-crm/actuator/metrics/gym_crm_user_registrations_total](http://localhost:8080/gym-crm/actuator/metrics/gym_crm_user_registrations_total)
  * Tags: `role` (trainee, trainer), `status` (success, failure)
* **Login Attempts (`gym_crm_login_attempts_total`)**: [http://localhost:8080/gym-crm/actuator/metrics/gym_crm_login_attempts_total](http://localhost:8080/gym-crm/actuator/metrics/gym_crm_login_attempts_total)
  * Tags: `status` (success, failure)
* **Trainings Created (`gym_crm_trainings_created_total`)**: [http://localhost:8080/gym-crm/actuator/metrics/gym_crm_trainings_created_total](http://localhost:8080/gym-crm/actuator/metrics/gym_crm_trainings_created_total)
  * Tags: `training_type`
  * *Note: Returns 404 until at least one training has been created.*
* **Training Search Duration (`gym_crm_search_duration_seconds`)**: [http://localhost:8080/gym-crm/actuator/metrics/gym_crm_search_duration_seconds](http://localhost:8080/gym-crm/actuator/metrics/gym_crm_search_duration_seconds)
  * Tags: `search_type` (trainee, trainer)
  * *Note: Returns 404 until at least one training search has been performed.*
* **Active Users (`gym_crm_active_users`)**: [http://localhost:8080/gym-crm/actuator/metrics/gym_crm_active_users](http://localhost:8080/gym-crm/actuator/metrics/gym_crm_active_users)
  * Tags: `role` (trainee, trainer)
