# BDD Component Tests

Black-box Cucumber tests for deployed service images. The module has no dependency on production Java modules or
production contract classes.

## Prerequisites

- Java 21
- Maven
- Docker

Docker must be running before executing the tests.

## Build the Gym Core image

The tests consume an existing OCI image. They do not build Gym Core.

### Fresh checkout

Build Gym Core and its reactor dependencies, then build the image from the packaged JAR:

```powershell
mvn package -pl gym-core-service -am
mvn spring-boot:build-image-no-fork -pl gym-core-service
```

`package -am` builds `gym-core-service`, `common`, and `workload-contract`. `build-image-no-fork` reuses the packaged
JAR instead of running the package lifecycle again.

### Dependencies already installed

When Gym Core dependencies are available in the local Maven repository, one command is sufficient:

```powershell
mvn spring-boot:build-image -pl gym-core-service
```

`build-image` forks the Maven package lifecycle before creating the image.

### Image name

The default image is:

```text
gym-core-service:local
```

Override it when building an image:

```powershell
mvn spring-boot:build-image -pl gym-core-service `
  "-DgymCore.image=gym-core-service:abc123"
```

Use the same property when running tests against an overridden image:

```powershell
mvn verify -pl gym-automation-tests "-DskipITs=false" `
  "-DgymCore.image=gym-core-service:abc123"
```

## Run BDD tests

Run every BDD scenario:

```powershell
mvn verify -pl gym-automation-tests "-DskipITs=false"
```

Component tests are disabled by default through `skipITs=true`. Enable them explicitly with `-DskipITs=false`.
Without that property, component tests remain skipped:

```powershell
mvn test
mvn verify
```

Surefire excludes `*Runner` classes by default. Failsafe is configured to execute `GymCoreComponentTestRunner` (matching `*TestRunner`) during
`integration-test` and check its results during `verify`.

## Focused execution

Use Cucumber tags to run a subset:

```powershell
mvn verify -pl gym-automation-tests "-DskipITs=false" "-Dcucumber.filter.tags=@happy-path"
mvn verify -pl gym-automation-tests "-DskipITs=false" "-Dcucumber.filter.tags=@authn"
mvn verify -pl gym-automation-tests "-DskipITs=false" "-Dcucumber.filter.tags=@authz"
mvn verify -pl gym-automation-tests "-DskipITs=false" "-Dcucumber.filter.tags=@validation"
```

Without `cucumber.filter.tags`, all scenarios selected by the runner execute.

## Runtime

The Gym Core suite starts one isolated Testcontainers stack for the test run:

- Gym Core from `gymCore.image`
- MySQL
- Redis
- ActiveMQ Classic

Scenarios run sequentially. Test data is provisioned through public registration and authentication APIs. Tests access
Gym Core only through HTTP and JMS contracts.
