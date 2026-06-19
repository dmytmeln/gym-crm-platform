# Discovery Server

This microservice acts as the Service Registry (Eureka Server) for the Gym CRM platform, enabling microservices to register themselves and discover each other.

## Setup and Running

### Run the Application

You can run the discovery server using Maven from the service directory or from the root:

**From the root directory:**
```bash
mvn spring-boot:run -pl discovery-server
```

**From the service directory:**
```bash
cd discovery-server
mvn spring-boot:run
```

The application runs on port 8761.

* **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
* **Eureka Service URL**: [http://localhost:8761/eureka/](http://localhost:8761/eureka/)

## Running Tests

To run unit and integration tests for this service:

```bash
mvn test -pl discovery-server
```

To run a specific test class:

```bash
mvn test -pl discovery-server -Dtest=DiscoveryServerApplicationTests
```
