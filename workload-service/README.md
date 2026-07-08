# Workload Service

This microservice handles managing and tracking trainer workloads.

## Prerequisites

In addition to the global prerequisites, you will need:
* ActiveMQ broker
* MongoDB instance
* Or Docker to run both

## Setup and Running

### Step 1: Set Up ActiveMQ

This service consumes trainer workload update messages from ActiveMQ. Follow the shared broker setup in the [root README](../README.md#step-3-start-shared-infrastructure).

Local profile defaults to:

* **Broker URL**: `failover:(tcp://localhost:61616)`
* **Username**: `gym`
* **Password**: `gym`

If your broker credentials differ, override via `SPRING_ACTIVEMQ_USER` / `SPRING_ACTIVEMQ_PASSWORD` environment variables.

### Step 2: Set Up MongoDB

This service persists trainer workload summaries in MongoDB. You can either use a locally installed MongoDB instance or run it using Docker.

#### Option A: Local MongoDB
Ensure your local MongoDB instance is running, and you have configured the database `gym_workload` with user credentials `gym`/`gym`.

#### Option B: Using Docker
You can start a MongoDB container with the required database and user setup using this command:

```bash
docker run --name gym-mongodb -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=gym -e MONGO_INITDB_ROOT_PASSWORD=gym -d mongo:latest
```

Local profile defaults to:

* **URI**: `mongodb://gym:gym@localhost:27017/gym_workload?authSource=admin`
* **Username**: `gym`
* **Password**: `gym`

If your MongoDB credentials or URI differ, override via `SPRING_DATA_MONGODB_URI` environment variable.


### Step 3: Run the Application

> [!IMPORTANT]
> The following services must be running for this service to register and function correctly:
> * **[Discovery Server](../discovery-server/README.md)**: Service registry required for Eureka registration.
> * **ActiveMQ broker**: Required for JMS message consumption.
> * **MongoDB**: Required for workload data persistence.

You can run the workload service using Maven from the service directory or from the root:

**From the root directory:**
```bash
mvn spring-boot:run -pl workload-service
```

**From the service directory:**
```bash
cd workload-service
mvn spring-boot:run
```

The application runs on port 8081 under the context path `/gym-crm/workload`.

* **Base API Path**: [http://localhost:8081/gym-crm/workload/api/v1](http://localhost:8081/gym-crm/workload/api/v1)
* **OpenAPI / Swagger UI**: [http://localhost:8081/gym-crm/workload/swagger-ui/index.html](http://localhost:8081/gym-crm/workload/swagger-ui/index.html)
* **OpenAPI Spec (JSON)**: [http://localhost:8081/gym-crm/workload/v3/api-docs](http://localhost:8081/gym-crm/workload/v3/api-docs)
* **OpenAPI Spec (YAML)**: [src/main/resources/gym-workload-api.yml](src/main/resources/gym-workload-api.yml) (can be loaded/edited in [Swagger Editor](https://editor.swagger.io/))

## Running Tests

To run unit and integration tests for this service:

```bash
mvn test -pl workload-service
```

To run a specific test class:

```bash
mvn test -pl workload-service -Dtest=WorkloadServiceApplicationTest
```

## Actuators and Metrics

Spring Boot Actuator and Micrometer are configured to expose system and custom metrics.

### Endpoints

* **Base Actuator Path**: [http://localhost:8081/gym-crm/workload/actuator](http://localhost:8081/gym-crm/workload/actuator)

#### Health Indicators
* **Main Health Status**: [http://localhost:8081/gym-crm/workload/actuator/health](http://localhost:8081/gym-crm/workload/actuator/health)
