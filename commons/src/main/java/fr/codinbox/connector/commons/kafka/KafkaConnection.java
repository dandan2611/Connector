package fr.codinbox.connector.commons.kafka;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * Represents a managed Kafka connection that exposes the loaded configuration properties
 * and provides factory methods for creating Kafka clients.
 *
 * <p>Unlike other connector types, Kafka connections do not maintain a persistent connection.
 * Instead, they load and validate a {@code .properties} file and provide factory methods
 * for creating {@link KafkaProducer}, {@link KafkaConsumer}, and {@link AdminClient} instances.</p>
 *
 * <p>All clients created through factory methods are tracked and will be closed gracefully
 * when {@link #shutdown()} is called.</p>
 *
 * <p>Configuration is loaded from a {@code .properties} file referenced by the
 * {@code CONNECTOR_KAFKA_<ID>_CONFIG} environment variable. The {@code bootstrap.servers}
 * property is required and validated at initialization. An optional {@code shutdownTimeoutMs}
 * property controls the graceful shutdown timeout (default: {@code 30000}).</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * KafkaConnection conn = service.getConnection("MAIN").orElseThrow();
 *
 * // No-arg: defaults to String serialization
 * KafkaProducer<String, String> producer = conn.createProducer();
 *
 * // With overrides for custom serializers or consumer group
 * Properties overrides = new Properties();
 * overrides.put("group.id", "my-group");
 * KafkaConsumer<String, String> consumer = conn.createConsumer(overrides);
 * }</pre>
 *
 * @see KafkaConnectorService
 */
public interface KafkaConnection {

    /**
     * Returns a defensive copy of the loaded Kafka properties.
     *
     * <p>Modifications to the returned {@link Properties} object do not affect the
     * internal configuration.</p>
     *
     * @return a copy of the Kafka configuration properties
     */
    @NotNull Properties getProperties();

    /**
     * Creates a new {@link KafkaProducer} with {@code String} key and value serializers.
     *
     * <p>The returned producer is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @return a new Kafka producer configured with the loaded properties
     */
    @NotNull KafkaProducer<String, String> createProducer();

    /**
     * Creates a new {@link KafkaProducer} with the specified property overrides.
     *
     * <p>Override properties are merged on top of the base configuration (overrides win).
     * The returned producer is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @param overrides additional properties to merge (e.g., custom serializers)
     * @param <K>       the key type
     * @param <V>       the value type
     * @return a new Kafka producer
     */
    @NotNull <K, V> KafkaProducer<K, V> createProducer(final @NotNull Properties overrides);

    /**
     * Creates a new {@link KafkaConsumer} with {@code String} key and value deserializers.
     *
     * <p>The returned consumer is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @return a new Kafka consumer configured with the loaded properties
     */
    @NotNull KafkaConsumer<String, String> createConsumer();

    /**
     * Creates a new {@link KafkaConsumer} with the specified property overrides.
     *
     * <p>Override properties are merged on top of the base configuration (overrides win).
     * The returned consumer is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @param overrides additional properties to merge (e.g., {@code group.id}, custom deserializers)
     * @param <K>       the key type
     * @param <V>       the value type
     * @return a new Kafka consumer
     */
    @NotNull <K, V> KafkaConsumer<K, V> createConsumer(final @NotNull Properties overrides);

    /**
     * Creates a new {@link AdminClient} using the base configuration properties.
     *
     * <p>The returned client is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @return a new Kafka admin client
     */
    @NotNull AdminClient createAdminClient();

    /**
     * Creates a new {@link AdminClient} with the specified property overrides.
     *
     * <p>The returned client is tracked and will be closed on {@link #shutdown()}.</p>
     *
     * @param overrides additional properties to merge
     * @return a new Kafka admin client
     */
    @NotNull AdminClient createAdminClient(final @NotNull Properties overrides);

    /**
     * Initializes the connection by loading and validating the configuration properties.
     *
     * @throws ConnectionInitException if the properties file cannot be loaded or
     *                                  {@code bootstrap.servers} is missing
     */
    void init() throws ConnectionInitException;

    /**
     * Shuts down all tracked Kafka clients gracefully with the configured timeout.
     *
     * <p>The timeout is controlled by the {@code shutdownTimeoutMs} property
     * (default: {@code 30000} milliseconds).</p>
     */
    void shutdown();

    /**
     * Returns whether this connection is configured to cause server shutdown on initialization failure.
     *
     * @return {@code true} if the server should exit when this connection fails to initialize
     */
    boolean isExitOnFailure();
}
