FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY workload-contract/pom.xml workload-contract/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY gym-core-service/pom.xml gym-core-service/pom.xml
COPY workload-service/pom.xml workload-service/pom.xml
COPY gym-automation-tests/pom.xml gym-automation-tests/pom.xml

RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn -B -pl gym-core-service -am dependency:go-offline

COPY common/src common/src
COPY workload-contract/src workload-contract/src
COPY gym-core-service/src gym-core-service/src

RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn -B -pl gym-core-service -am package -DskipTests && \
    java -Djarmode=tools -jar gym-core-service/target/*.jar extract --layers --destination extracted && \
    mv extracted/application/*.jar extracted/application/application.jar

FROM eclipse-temurin:21.0.11_10-jre-alpine

RUN addgroup -S -g 1000 app && \
    adduser -S -u 1000 -G app app && \
    mkdir -p /application/logs && \
    chown -R app:app /application

WORKDIR /application

COPY --chown=app:app --from=build /workspace/extracted/dependencies/ ./
COPY --chown=app:app --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --chown=app:app --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --chown=app:app --from=build /workspace/extracted/application/ ./

USER app

RUN java -XX:ArchiveClassesAtExit=application.jsa \
    -Dspring.context.exit=onRefresh \
    -Dspring.liquibase.enabled=false \
    -Dspring.jpa.hibernate.ddl-auto=none \
    -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect \
    -Dspring.datasource.hikari.initialization-fail-timeout=-1 \
    -Dspring.datasource.hikari.connection-timeout=3000 \
    -Deureka.client.enabled=false \
    -Dspring.jms.listener.auto-startup=false \
    -jar application.jar

EXPOSE 8082

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -XX:SharedArchiveFile=/application/application.jsa"
ENTRYPOINT ["java", "-jar", "application.jar"]
