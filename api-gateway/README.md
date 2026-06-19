# API Gateway

This microservice acts as the API Gateway for the Gym CRM platform, routing requests to the appropriate downstream microservices (such as Core Service and Workload Service) via Eureka Service Discovery.

## Setup and Running

### Run the Application

> [!IMPORTANT]
> The following service must be running for this service to register and function correctly:
> * **[Discovery Server](../discovery-server/README.md)**: Service registry required for Eureka registration.

You can run the API gateway using Maven from the service directory or from the root:

**From the root directory:**
```bash
mvn spring-boot:run -pl api-gateway
```

**From the service directory:**
```bash
cd api-gateway
mvn spring-boot:run
```

The application runs on port 8080.

* **Base URL**: [http://localhost:8080](http://localhost:8080)
* **Gym Core Route**: `/gym-crm/core/**` -> `gym-core-service`
* **Workload Route**: `/gym-crm/workload/**` -> `workload-service`

## Running Tests

To run unit tests for this service:

```bash
mvn test -pl api-gateway
```

To run a specific test class:

```bash
mvn test -pl api-gateway -Dtest=ApiGatewayApplicationTests
```

## Actuators and Metrics

Spring Boot Actuator and Micrometer are configured to expose system metrics.

### Endpoints

* **Base Actuator Path**: [http://localhost:8080/actuator](http://localhost:8080/actuator)

#### Health Indicators
* **Main Health Status**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
