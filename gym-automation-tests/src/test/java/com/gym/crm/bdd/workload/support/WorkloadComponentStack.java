package com.gym.crm.bdd.workload.support;

import lombok.NoArgsConstructor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class WorkloadComponentStack {

    public static final String QUEUE = "component.trainer.workload.updates";
    public static final String JWT_SECRET_BASE64 = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final int MONGODB_PORT = 27017;
    private static final int ACTIVEMQ_PORT = 61616;
    private static final int ACTIVEMQ_WEB_PORT = 8161;
    private static final int WORKLOAD_PORT = 8081;
    private static final int HTTP_OK = 200;
    private static final int STARTUP_TIMEOUT_MINUTES = 3;
    private static final String MONGODB_DATABASE = "gym_workload";
    private static final String MONGODB_ALIAS = "mongodb";
    private static final String ACTIVEMQ_ALIAS = "activemq";
    private static final String ACTIVEMQ_USER = "admin";
    private static final String ACTIVEMQ_PASSWORD = "admin";
    private static Network network;
    private static MongoDBContainer mongodb;
    private static GenericContainer<?> activeMQ;
    private static GenericContainer<?> workload;

    public static void start() {
        if (workload != null) {
            return;
        }

        initializeContainers();

        try {
            Startables.deepStart(Stream.of(mongodb, activeMQ)).join();

            workload = createWorkloadService();
            workload.start();
        } catch (RuntimeException exception) {
            stop();
            throw exception;
        }
    }

    public static String baseUrl() {
        return "http://" + workload.getHost() + ":" + workload.getMappedPort(WORKLOAD_PORT) + "/gym-crm/workload/api/v1";
    }

    public static String brokerUrl() {
        return "tcp://" + activeMQ.getHost() + ":" + activeMQ.getMappedPort(ACTIVEMQ_PORT);
    }

    public static void stop() {
        stopContainer(workload);
        stopContainer(activeMQ);
        stopContainer(mongodb);

        if (network != null) {
            network.close();
        }

        workload = null;
        activeMQ = null;
        mongodb = null;
        network = null;
    }

    private static void initializeContainers() {
        network = Network.newNetwork();
        mongodb = createMongoDBContainer();
        activeMQ = createActiveMQContainer();
    }

    @SuppressWarnings("resource")
    private static MongoDBContainer createMongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:7.0.15"))
                .withNetwork(network)
                .withNetworkAliases(MONGODB_ALIAS)
                .withReuse(false);
    }

    @SuppressWarnings("resource")
    private static GenericContainer<?> createActiveMQContainer() {
        return new GenericContainer<>("apache/activemq-classic:5.18.6")
                .withExposedPorts(ACTIVEMQ_PORT, ACTIVEMQ_WEB_PORT)
                .withNetwork(network)
                .withNetworkAliases(ACTIVEMQ_ALIAS)
                .waitingFor(new WaitAllStrategy()
                        .withStrategy(Wait.forListeningPort())
                        .withStrategy(Wait.forHttp("/")
                                .forPort(ACTIVEMQ_WEB_PORT)
                                .withBasicCredentials(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD)
                                .forStatusCode(HTTP_OK)));
    }

    @SuppressWarnings("resource")
    private static GenericContainer<?> createWorkloadService() {
        return new GenericContainer<>(DockerImageName.parse(System.getProperty("workload.image", "workload-service:local")))
                .withExposedPorts(WORKLOAD_PORT)
                .withNetwork(network)
                .withEnv(createWorkloadEnvironment())
                .waitingFor(Wait.forHttp("/gym-crm/workload/actuator/health")
                        .forStatusCode(HTTP_OK)
                        .withStartupTimeout(Duration.ofMinutes(STARTUP_TIMEOUT_MINUTES)));
    }

    private static Map<String, String> createWorkloadEnvironment() {
        return Map.ofEntries(entry("SPRING_PROFILES_ACTIVE", "local"),
                entry("SPRING_DATA_MONGODB_URI",
                        "mongodb://" + MONGODB_ALIAS + ":" + MONGODB_PORT + "/" + MONGODB_DATABASE),
                entry("SPRING_ACTIVEMQ_BROKER_URL", "tcp://" + ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT),
                entry("SPRING_ACTIVEMQ_USER", ACTIVEMQ_USER),
                entry("SPRING_ACTIVEMQ_PASSWORD", ACTIVEMQ_PASSWORD),
                entry("EUREKA_CLIENT_ENABLED", "false"),
                entry("APP_LOGGING_PATH", "/tmp/logs"),
                entry("APP_JMS_QUEUES_TRAINER_WORKLOAD", QUEUE),
                entry("APP_SECURITY_JWT_SECRET_KEY", JWT_SECRET_BASE64));
    }

    private static void stopContainer(GenericContainer<?> container) {
        if (container == null) {
            return;
        }

        container.stop();
    }

}
