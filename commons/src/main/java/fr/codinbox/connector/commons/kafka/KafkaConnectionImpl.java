package fr.codinbox.connector.commons.kafka;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.utils.EnvUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link KafkaConnection} that loads Kafka properties from a file,
 * provides factory methods for creating clients, and tracks them for graceful shutdown.
 *
 * @see KafkaConnection
 */
public class KafkaConnectionImpl implements KafkaConnection {

    private static final long DEFAULT_SHUTDOWN_TIMEOUT_MS = 30000;

    private final @NotNull Logger logger;
    private final @NotNull String id;
    private final @NotNull String configFilePath;

    private @Nullable Properties properties;
    private long shutdownTimeoutMs = DEFAULT_SHUTDOWN_TIMEOUT_MS;
    private final @NotNull List<AutoCloseable> trackedClients = new CopyOnWriteArrayList<>();

    /**
     * Creates a new Kafka connection instance.
     *
     * @param logger         the logger for diagnostic messages
     * @param id             the connection identifier
     * @param configFilePath the path to the {@code .properties} configuration file
     */
    public KafkaConnectionImpl(final @NotNull Logger logger,
                               final @NotNull String id,
                               final @NotNull String configFilePath) {
        this.logger = logger;
        this.id = id;
        this.configFilePath = configFilePath;
    }

    @Override
    public void init() throws ConnectionInitException {
        this.properties = new Properties();
        try (var fis = new FileInputStream(this.configFilePath)) {
            this.properties.load(fis);
        } catch (Exception e) {
            throw new ConnectionInitException(e);
        }

        if (!this.properties.containsKey("bootstrap.servers")) {
            throw new ConnectionInitException("Missing required property 'bootstrap.servers' in Kafka config: " + this.configFilePath);
        }

        final String timeoutStr = this.properties.getProperty("shutdownTimeoutMs");
        if (timeoutStr != null) {
            this.shutdownTimeoutMs = Long.parseLong(timeoutStr);
            this.properties.remove("shutdownTimeoutMs");
        }

        logger.info(id + ": Kafka configuration loaded (bootstrap.servers=" +
                this.properties.getProperty("bootstrap.servers") + ")");
    }

    @Override
    public @NotNull Properties getProperties() {
        final var copy = new Properties();
        if (this.properties != null) {
            copy.putAll(this.properties);
        }
        return copy;
    }

    private @NotNull Properties mergeProperties(final @NotNull Properties overrides) {
        final var merged = getProperties();
        merged.putAll(overrides);
        return merged;
    }

    @Override
    public @NotNull KafkaProducer<String, String> createProducer() {
        final var props = getProperties();
        props.putIfAbsent("key.serializer", StringSerializer.class.getName());
        props.putIfAbsent("value.serializer", StringSerializer.class.getName());
        final var producer = new KafkaProducer<String, String>(props);
        trackedClients.add(producer);
        return producer;
    }

    @Override
    public @NotNull <K, V> KafkaProducer<K, V> createProducer(final @NotNull Properties overrides) {
        final var props = mergeProperties(overrides);
        final var producer = new KafkaProducer<K, V>(props);
        trackedClients.add(producer);
        return producer;
    }

    @Override
    public @NotNull KafkaConsumer<String, String> createConsumer() {
        final var props = getProperties();
        props.putIfAbsent("key.deserializer", StringDeserializer.class.getName());
        props.putIfAbsent("value.deserializer", StringDeserializer.class.getName());
        final var consumer = new KafkaConsumer<String, String>(props);
        trackedClients.add(consumer);
        return consumer;
    }

    @Override
    public @NotNull <K, V> KafkaConsumer<K, V> createConsumer(final @NotNull Properties overrides) {
        final var props = mergeProperties(overrides);
        final var consumer = new KafkaConsumer<K, V>(props);
        trackedClients.add(consumer);
        return consumer;
    }

    @Override
    public @NotNull AdminClient createAdminClient() {
        final var client = AdminClient.create(getProperties());
        trackedClients.add(client);
        return client;
    }

    @Override
    public @NotNull AdminClient createAdminClient(final @NotNull Properties overrides) {
        final var client = AdminClient.create(mergeProperties(overrides));
        trackedClients.add(client);
        return client;
    }

    @Override
    public void shutdown() {
        final var timeout = Duration.ofMillis(this.shutdownTimeoutMs);
        for (AutoCloseable client : trackedClients) {
            try {
                if (client instanceof KafkaProducer<?, ?> producer) {
                    producer.close(timeout);
                } else if (client instanceof KafkaConsumer<?, ?> consumer) {
                    consumer.close(timeout);
                } else {
                    client.close();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, id + ": Error closing Kafka client", e);
            }
        }
        trackedClients.clear();
    }

    @Override
    public boolean isExitOnFailure() {
        return EnvUtils.isExitOnFailure(ConnectionType.KAFKA, this.id);
    }
}
