# Gym Core Service

This microservice handles the core logic for the Gym CRM application, including user authentication, trainee and trainer profiles, and training sessions.

## Prerequisites

In addition to the global prerequisites, you will need:
* MySQL Server (8.0+) or Docker (to run MySQL inside a container)
* Redis Server (6.0+) or Docker (to run Redis inside a container)
* ActiveMQ broker or Docker

## Setup and Running

### Step 1: Set Up the Database

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

> Liquibase will generate database tables and initial schemas automatically on application startup.

### Step 2: Set Up Redis

You can either use a locally installed Redis server or run it using Docker.

#### Option A: Local Redis Server
Start your local Redis service on the default port (`6379`).

#### Option B: Using Docker
If you prefer Docker, you can start a Redis container with the default port setup using this command:

```bash
docker run --name gym-redis -p 6379:6379 -d redis
```

### Step 3: Set Up ActiveMQ

This service publishes trainer workload update messages to ActiveMQ. Follow the shared broker setup in the [root README](../README.md#step-3-start-shared-infrastructure).

Local profile defaults to:

* **Broker URL**: `failover:(tcp://localhost:61616)`
* **Username**: `gym`
* **Password**: `gym`

If your broker credentials differ from the local profile defaults above, override service credentials at startup via
`SPRING_ACTIVEMQ_USER` / `SPRING_ACTIVEMQ_PASSWORD` or equivalent Spring Boot arguments or change ActiveMQ username and password for user.

### Step 4: Run the Application

> [!IMPORTANT]
> The following platform microservices must be running for this service to function properly:
> * **[Discovery Server](../discovery-server/README.md)**: Service registry required for Eureka discovery.
> * **[Workload Service](../workload-service/README.md)**: Handles trainer workloads (consumes trainer workload updates from ActiveMQ).
> * **ActiveMQ broker**: Required for JMS message publishing.
>
> Ensure these services are started before running the core service.

You can run the core service using Maven from the service directory or from the root:

**From the root directory:**
```bash
mvn spring-boot:run -pl gym-core-service
```

**From the service directory:**
```bash
cd gym-core-service
mvn spring-boot:run
```

The application runs on port 8082 under the context path `/gym-crm/core`.

* **Base API Path**: [http://localhost:8082/gym-crm/core/api/v1](http://localhost:8082/gym-crm/core/api/v1)
* **OpenAPI / Swagger UI**: [http://localhost:8082/gym-crm/core/swagger-ui/index.html](http://localhost:8082/gym-crm/core/swagger-ui/index.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8082/gym-crm/core/v3/api-docs](http://localhost:8082/gym-crm/core/v3/api-docs)
* **OpenAPI Spec (YAML)**: [src/main/resources/gia-api.yml](src/main/resources/gym-core-api.yml) (can be loaded/edited in [Swagger Editor](https://editor.swagger.io/))

## Running Tests

To run unit and integration tests for this service:

```bash
mvn test -pl gym-core-service
```

To run a specific test class:

```bash
mvn test -pl gym-core-service -Dtest=GymCrmApplicationTest
```

## Actuators and Metrics

Spring Boot Actuator and Micrometer are configured to expose system and custom metrics.

### Endpoints

* **Base Actuator Path**: [http://localhost:8082/gym-crm/core/actuator](http://localhost:8082/gym-crm/core/actuator)

#### Health Indicators
* **Main Health Status**: [http://localhost:8082/gym-crm/core/actuator/health](http://localhost:8082/gym-crm/core/actuator/health)
* **Database Health**: [http://localhost:8082/gym-crm/core/actuator/health/db](http://localhost:8082/gym-crm/core/actuator/health/db)
* **Disk Write Health (Custom)**: [http://localhost:8082/gym-crm/core/actuator/health/diskWrite](http://localhost:8082/gym-crm/core/actuator/health/diskWrite)
* **Hikari Connection Pool Saturation (Custom)**: [http://localhost:8082/gym-crm/core/actuator/health/hikariPoolSaturation](http://localhost:8082/gym-crm/core/actuator/health/hikariPoolSaturation)

#### Metrics
* **Prometheus Metrics**: [http://localhost:8082/gym-crm/core/actuator/prometheus](http://localhost:8082/gym-crm/core/actuator/prometheus)
* **Metrics Index**: [http://localhost:8082/gym-crm/core/actuator/metrics](http://localhost:8082/gym-crm/core/actuator/metrics)

### Custom Metrics API

* **User Registrations (`gym_crm_user_registrations_total`)**: [http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_user_registrations_total](http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_user_registrations_total)
  * Tags: `role` (trainee, trainer), `status` (success, failure)
* **Login Attempts (`gym_crm_login_attempts_total`)**: [http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_login_attempts_total](http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_login_attempts_total)
  * Tags: `status` (success, failure)
* **Trainings Created (`gym_crm_trainings_created_total`)**: [http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_trainings_created_total](http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_trainings_created_total)
  * Tags: `training_type`
  * *Note: Returns 404 until at least one training has been created.*
* **Training Search Duration (`gym_crm_search_duration_seconds`)**: [http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_search_duration_seconds](http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_search_duration_seconds)
  * Tags: `search_type` (trainee, trainer)
  * *Note: Returns 404 until at least one training search has been performed.*
* **Active Users (`gym_crm_active_users`)**: [http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_active_users](http://localhost:8082/gym-crm/core/actuator/metrics/gym_crm_active_users)
  * Tags: `role` (trainee, trainer)
