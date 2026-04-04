package fr.codinbox.connector.velocity;

import fr.codinbox.connector.commons.database.DatabaseConnectorService;
import fr.codinbox.connector.commons.kafka.KafkaConnectorService;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorService;
import fr.codinbox.connector.commons.redis.RedisConnectorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Static service locator for accessing connector services on the Velocity platform.
 *
 * <p>This class provides global access to all four connector services. Services are
 * initialized by the Velocity {@code ConnectorPlugin} at startup and should not be set externally.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * RedisConnectorService redis = Connector.getRedisService();
 * DatabaseConnectorService db = Connector.getDatabaseService();
 * RabbitMQConnectorService rabbit = Connector.getRabbitMQService();
 * KafkaConnectorService kafka = Connector.getKafkaService();
 * }</pre>
 */
public class Connector {

    private static @Nullable DatabaseConnectorService databaseConnectorService = null;
    private static @Nullable RedisConnectorService redisConnectorService = null;
    private static @Nullable RabbitMQConnectorService rabbitMQConnectorService = null;
    private static @Nullable KafkaConnectorService kafkaConnectorService = null;

    /**
     * Returns the database connector service.
     *
     * @return the initialized {@link DatabaseConnectorService}
     * @throws NullPointerException if the service has not been initialized
     */
    public static @NotNull DatabaseConnectorService getDatabaseService() {
        return Objects.requireNonNull(databaseConnectorService, "DatabaseConnectorService has not been initialized");
    }

    /**
     * Returns the Redis connector service.
     *
     * @return the initialized {@link RedisConnectorService}
     * @throws NullPointerException if the service has not been initialized
     */
    public static @NotNull RedisConnectorService getRedisService() {
        return Objects.requireNonNull(redisConnectorService, "RedisConnectorService has not been initialized");
    }

    /**
     * Returns the RabbitMQ connector service.
     *
     * @return the initialized {@link RabbitMQConnectorService}
     * @throws NullPointerException if the service has not been initialized
     */
    public static @NotNull RabbitMQConnectorService getRabbitMQService() {
        return Objects.requireNonNull(rabbitMQConnectorService, "RabbitMQConnectorService has not been initialized");
    }

    /**
     * Returns the Kafka connector service.
     *
     * @return the initialized {@link KafkaConnectorService}
     * @throws NullPointerException if the service has not been initialized
     */
    public static @NotNull KafkaConnectorService getKafkaService() {
        return Objects.requireNonNull(kafkaConnectorService, "KafkaConnectorService has not been initialized");
    }

    protected static void setDatabaseService(final @NotNull DatabaseConnectorService service) {
        if (databaseConnectorService != null)
            throw new IllegalStateException("DatabaseConnectorService has already been initialized");
        databaseConnectorService = service;
    }

    protected static void setRedisService(final @NotNull RedisConnectorService service) {
        if (redisConnectorService != null)
            throw new IllegalStateException("RedisConnectorService has already been initialized");
        redisConnectorService = service;
    }

    protected static void setRabbitMQService(final @NotNull RabbitMQConnectorService service) {
        if (rabbitMQConnectorService != null)
            throw new IllegalStateException("RabbitMQConnectorService has already been initialized");
        rabbitMQConnectorService = service;
    }

    protected static void setKafkaService(final @NotNull KafkaConnectorService service) {
        if (kafkaConnectorService != null)
            throw new IllegalStateException("KafkaConnectorService has already been initialized");
        kafkaConnectorService = service;
    }
}
