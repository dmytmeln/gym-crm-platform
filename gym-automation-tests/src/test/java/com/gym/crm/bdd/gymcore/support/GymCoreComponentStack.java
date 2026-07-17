package com.gym.crm.bdd.gymcore.support;

import com.redis.testcontainers.RedisContainer;
import lombok.NoArgsConstructor;
import org.testcontainers.containers.GenericContainer;
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
public final class GymCoreComponentStack {

    public static final String QUEUE = "component.trainer.workload.updates";
    private static final int MYSQL_PORT = 3306;
    private static final int ACTIVEMQ_PORT = 61616;
    private static final int ACTIVEMQ_WEB_PORT = 8161;
    private static final int GYM_CORE_PORT = 8082;
    private static final int HTTP_OK = 200;
    private static final int STARTUP_TIMEOUT_MINUTES = 3;
    private static final String MYSQL_DB = "gym_db";
    private static final String MYSQL_USER = "gym";
    private static final String MYSQL_PASSWORD = "gym";
    private static final String MYSQL_ALIAS = "mysql";
    private static final String REDIS_ALIAS = "redis";
    private static final String ACTIVEMQ_ALIAS = "activemq";
    private static final String ACTIVEMQ_USER = "admin";
    private static final String ACTIVEMQ_PASSWORD = "admin";

    private static Network network;
    private static MySQLContainer<?> mysql;
    private static RedisContainer redis;
    private static GenericContainer<?> activeMQ;
    private static GenericContainer<?> gymCore;

    public static void start() {
        if (gymCore != null) {
            return;
        }

        initializeContainers();
        startContainers();
    }

    public static String baseUrl() {
        return "http://" + gymCore.getHost() + ":" + gymCore.getMappedPort(GYM_CORE_PORT) + "/gym-crm/core/api/v1";
    }

    public static String brokerUrl() {
        return "tcp://" + activeMQ.getHost() + ":" + activeMQ.getMappedPort(ACTIVEMQ_PORT);
    }

    public static void stop() {
        stopContainer(gymCore);
        stopContainer(activeMQ);
        stopContainer(redis);
        stopContainer(mysql);

        if (network != null) {
            network.close();
        }

        gymCore = null;
        activeMQ = null;
        redis = null;
        mysql = null;
        network = null;
    }

    private static void initializeContainers() {
        network = Network.newNetwork();
        mysql = createMySQLContainer();
        redis = createRedisContainer();
        activeMQ = createActiveMQContainer();
    }

    private static void startContainers() {
        try {
            Startables.deepStart(Stream.of(mysql, redis, activeMQ)).join();

            gymCore = createGymCoreService();
            gymCore.start();
        } catch (RuntimeException exception) {
            stop();
            throw exception;
        }
    }

    @SuppressWarnings("resource")
    private static MySQLContainer<?> createMySQLContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4.3"))
                .withDatabaseName(MYSQL_DB)
                .withUsername(MYSQL_USER)
                .withPassword(MYSQL_PASSWORD)
                .withNetwork(network)
                .withNetworkAliases(MYSQL_ALIAS)
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
                .withEnv(createGymCoreEnvironment())
                .waitingFor(Wait.forHttp("/gym-crm/core/actuator/health")
                        .forStatusCode(HTTP_OK)
                        .withStartupTimeout(Duration.ofMinutes(STARTUP_TIMEOUT_MINUTES)));
    }

    private static Map<String, String> createGymCoreEnvironment() {
        return Map.ofEntries(entry("SPRING_PROFILES_ACTIVE", "local"),
                entry("SPRING_DATASOURCE_URL", "jdbc:mysql://" + MYSQL_ALIAS + ":" + MYSQL_PORT + "/" + MYSQL_DB),
                entry("SPRING_DATASOURCE_USERNAME", MYSQL_USER),
                entry("SPRING_DATASOURCE_PASSWORD", MYSQL_PASSWORD),
                entry("SPRING_DATA_REDIS_HOST", REDIS_ALIAS),
                entry("SPRING_ACTIVEMQ_BROKER_URL", "tcp://" + ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT),
                entry("SPRING_ACTIVEMQ_USER", ACTIVEMQ_USER),
                entry("SPRING_ACTIVEMQ_PASSWORD", ACTIVEMQ_PASSWORD),
                entry("EUREKA_CLIENT_ENABLED", "false"),
                entry("APP_LOGGING_PATH", "/tmp/logs"),
                entry("APP_JMS_QUEUES_TRAINER_WORKLOAD", QUEUE));
    }

    private static void stopContainer(GenericContainer<?> container) {
        if (container == null) {
            return;
        }

        container.stop();
    }

}
