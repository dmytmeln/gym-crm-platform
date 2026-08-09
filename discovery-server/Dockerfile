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
    mvn -B -pl discovery-server -am dependency:go-offline

COPY discovery-server/src discovery-server/src

RUN --mount=type=cache,id=m2-cache,target=/root/.m2 \
    mvn -B -pl discovery-server -am package -DskipTests && \
    java -Djarmode=tools -jar discovery-server/target/*.jar extract --layers --destination extracted && \
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
    -jar application.jar

EXPOSE 8761

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -XX:SharedArchiveFile=/application/application.jsa"
ENTRYPOINT ["java", "-jar", "application.jar"]
