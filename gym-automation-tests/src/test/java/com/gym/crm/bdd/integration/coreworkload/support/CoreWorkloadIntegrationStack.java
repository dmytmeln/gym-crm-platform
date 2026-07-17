package com.gym.crm.bdd.integration.coreworkload.support;

import com.redis.testcontainers.RedisContainer;
import lombok.NoArgsConstructor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
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
public final class CoreWorkloadIntegrationStack {

    public static final String QUEUE = "integration.core-workload.trainer-workload";
    public static final String DEAD_LETTER_QUEUE = "integration.core-workload.trainer-workload.dlq";
    private static final String JWT_SECRET_BASE64 = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final int MYSQL_PORT = 3306;
    private static final int MONGODB_PORT = 27017;
    private static final int ACTIVEMQ_PORT = 61616;
    private static final int ACTIVEMQ_WEB_PORT = 8161;
    private static final int GYM_CORE_PORT = 8082;
    private static final int WORKLOAD_PORT = 8081;
    private static final int HTTP_OK = 200;
    private static final int STARTUP_TIMEOUT_MINUTES = 3;
    private static final String MYSQL_DATABASE = "gym_db";
    private static final String MYSQL_USER = "gym";
    private static final String MYSQL_PASSWORD = "gym";
    private static final String MONGODB_DATABASE = "gym_workload";
    private static final String MYSQL_ALIAS = "mysql";
    private static final String MONGODB_ALIAS = "mongodb";
    private static final String REDIS_ALIAS = "redis";
    private static final String ACTIVEMQ_ALIAS = "activemq";
    private static final String ACTIVEMQ_USER = "admin";
    private static final String ACTIVEMQ_PASSWORD = "admin";
    private static final String GYM_CORE_SERVICE_NAME = "Gym Core";
    private static final String WORKLOAD_SERVICE_NAME = "Workload";

    private static Network network;
    private static MySQLContainer<?> mysql;
    private static MongoDBContainer mongodb;
    private static RedisContainer redis;
    private static GenericContainer<?> activeMQ;
    private static GenericContainer<?> gymCore;
    private static GenericContainer<?> workload;

    public static void start() {
        if (gymCore != null) {
            return;
        }

        initializeContainers();
        startContainers();
    }

    public static String gymCoreBaseUrl() {
        return "http://" + gymCore.getHost() + ":" + gymCore.getMappedPort(GYM_CORE_PORT) + "/gym-crm/core/api/v1";
    }

    public static String workloadBaseUrl() {
        return "http://" + workload.getHost() + ":" + workload.getMappedPort(WORKLOAD_PORT) + "/gym-crm/workload/api/v1";
    }

    public static void printServiceLogs() {
        printContainerLogs(GYM_CORE_SERVICE_NAME, gymCore);
        printContainerLogs(WORKLOAD_SERVICE_NAME, workload);
    }

    public static void stop() {
        stopContainer(workload);
        stopContainer(gymCore);
        stopContainer(activeMQ);
        stopContainer(redis);
        stopContainer(mongodb);
        stopContainer(mysql);

        if (network != null) {
            network.close();
        }

        workload = null;
        gymCore = null;
        activeMQ = null;
        redis = null;
        mongodb = null;
        mysql = null;
        network = null;
    }

    private static void initializeContainers() {
        network = Network.newNetwork();
        mysql = createMySQLContainer();
        mongodb = createMongoDBContainer();
        redis = createRedisContainer();
        activeMQ = createActiveMQContainer();
    }

    private static void startContainers() {
        try {
            Startables.deepStart(Stream.of(mysql, mongodb, redis, activeMQ)).join();

            gymCore = createGymCoreService();
            workload = createWorkloadService();
            Startables.deepStart(Stream.of(gymCore, workload)).join();
        } catch (RuntimeException exception) {
            printServiceLogs();
            stop();

            throw exception;
        }
    }

    @SuppressWarnings("resource")
    private static MySQLContainer<?> createMySQLContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.3"))
                .withDatabaseName(MYSQL_DATABASE)
                .withUsername(MYSQL_USER)
                .withPassword(MYSQL_PASSWORD)
                .withNetwork(network)
                .withNetworkAliases(MYSQL_ALIAS)
                .withReuse(false);
    }

    @SuppressWarnings("resource")
    private static MongoDBContainer createMongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:7.0.15"))
                .withNetwork(network)
                .withNetworkAliases(MONGODB_ALIAS)
                .withReuse(false);
    }

    @SuppressWarnings("resource")
    private static RedisContainer createRedisContainer() {
        return new RedisContainer(DockerImageName.parse("redis:7.4.1-alpine"))
                .withNetwork(network)
                .withNetworkAliases(REDIS_ALIAS);
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
    private static GenericContainer<?> createGymCoreService() {
        return new GenericContainer<>(DockerImageName.parse(
                System.getProperty("gymCore.image", "gym-core-service:local")))
                .withExposedPorts(GYM_CORE_PORT)
                .withNetwork(network)
                .withEnv(gymCoreEnvironment())
                .waitingFor(Wait.forHttp("/gym-crm/core/actuator/health")
                        .forStatusCode(HTTP_OK)
                        .withStartupTimeout(Duration.ofMinutes(STARTUP_TIMEOUT_MINUTES)));
    }

    @SuppressWarnings("resource")
    private static GenericContainer<?> createWorkloadService() {
        return new GenericContainer<>(DockerImageName.parse(
                System.getProperty("workload.image", "workload-service:local")))
                .withExposedPorts(WORKLOAD_PORT)
                .withNetwork(network)
                .withEnv(workloadEnvironment())
                .waitingFor(Wait.forHttp("/gym-crm/workload/actuator/health")
                        .forStatusCode(HTTP_OK)
                        .withStartupTimeout(Duration.ofMinutes(STARTUP_TIMEOUT_MINUTES)));
    }

    private static Map<String, String> gymCoreEnvironment() {
        return Map.ofEntries(entry("SPRING_PROFILES_ACTIVE", "local"),
                entry("SPRING_DATASOURCE_URL", "jdbc:mysql://" + MYSQL_ALIAS + ":" + MYSQL_PORT + "/" + MYSQL_DATABASE),
                entry("SPRING_DATASOURCE_USERNAME", MYSQL_USER),
                entry("SPRING_DATASOURCE_PASSWORD", MYSQL_PASSWORD),
                entry("SPRING_DATA_REDIS_HOST", REDIS_ALIAS),
                entry("SPRING_ACTIVEMQ_BROKER_URL", brokerUrl()),
                entry("SPRING_ACTIVEMQ_USER", ACTIVEMQ_USER),
                entry("SPRING_ACTIVEMQ_PASSWORD", ACTIVEMQ_PASSWORD),
                entry("EUREKA_CLIENT_ENABLED", "false"),
                entry("APP_LOGGING_PATH", "/tmp/logs"),
                entry("APP_SECURITY_JWT_SECRET_KEY", JWT_SECRET_BASE64),
                entry("APP_JMS_QUEUES_TRAINER_WORKLOAD", QUEUE));
    }

    private static Map<String, String> workloadEnvironment() {
        return Map.ofEntries(entry("SPRING_PROFILES_ACTIVE", "local"),
                entry("SPRING_DATA_MONGODB_URI", "mongodb://" + MONGODB_ALIAS + ":" + MONGODB_PORT + "/" + MONGODB_DATABASE),
                entry("SPRING_ACTIVEMQ_BROKER_URL", brokerUrl()),
                entry("SPRING_ACTIVEMQ_USER", ACTIVEMQ_USER),
                entry("SPRING_ACTIVEMQ_PASSWORD", ACTIVEMQ_PASSWORD),
                entry("EUREKA_CLIENT_ENABLED", "false"),
                entry("APP_LOGGING_PATH", "/tmp/logs"),
                entry("APP_SECURITY_JWT_SECRET_KEY", JWT_SECRET_BASE64),
                entry("APP_JMS_QUEUES_TRAINER_WORKLOAD", QUEUE),
                entry("APP_JMS_QUEUES_TRAINER_WORKLOAD_DLQ", DEAD_LETTER_QUEUE));
    }

    private static String brokerUrl() {
        return "tcp://" + ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT;
    }

    private static void printContainerLogs(String serviceName, GenericContainer<?> container) {
        if (container == null) {
            return;
        }

        System.err.println("===== " + serviceName + " container logs =====");
        System.err.println(container.getLogs());
    }

    private static void stopContainer(GenericContainer<?> container) {
        if (container == null) {
            return;
        }

        container.stop();
    }

}
