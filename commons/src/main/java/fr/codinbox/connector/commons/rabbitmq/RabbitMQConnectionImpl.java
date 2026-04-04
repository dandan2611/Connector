package fr.codinbox.connector.commons.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.utils.EnvUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Implementation of {@link RabbitMQConnection} that manages a RabbitMQ connection
 * and a fixed-size channel pool.
 *
 * <p>The channel pool is pre-populated at initialization time. Channels are borrowed
 * and returned via {@link PooledChannel}, which implements {@link AutoCloseable}.</p>
 *
 * @see RabbitMQConnection
 * @see PooledChannel
 */
public class RabbitMQConnectionImpl implements RabbitMQConnection {

    private static final int DEFAULT_CHANNEL_POOL_SIZE = 5;

    private final @NotNull Logger logger;
    private final @NotNull String id;
    private final @NotNull String configFilePath;

    private @Nullable com.rabbitmq.client.Connection connection;
    private @Nullable BlockingQueue<Channel> channelPool;

    /**
     * Creates a new RabbitMQ connection instance.
     *
     * @param logger         the logger for diagnostic messages
     * @param id             the connection identifier
     * @param configFilePath the path to the {@code .properties} configuration file
     */
    public RabbitMQConnectionImpl(final @NotNull Logger logger,
                                  final @NotNull String id,
                                  final @NotNull String configFilePath) {
        this.logger = logger;
        this.id = id;
        this.configFilePath = configFilePath;
    }

    @Override
    public void init() throws ConnectionInitException {
        final var props = new Properties();
        try (var fis = new FileInputStream(this.configFilePath)) {
            props.load(fis);
        } catch (Exception e) {
            throw new ConnectionInitException(e);
        }

        final var factory = new ConnectionFactory();
        factory.setHost(props.getProperty("host", "localhost"));
        factory.setPort(Integer.parseInt(props.getProperty("port", "5672")));
        factory.setUsername(props.getProperty("username", "guest"));
        factory.setPassword(props.getProperty("password", "guest"));
        factory.setVirtualHost(props.getProperty("virtualHost", "/"));

        if (EnvUtils.checkBool(props.getProperty("ssl", "false"))) {
            try {
                factory.useSslProtocol();
            } catch (Exception e) {
                throw new ConnectionInitException(e);
            }
        }

        logger.fine(id + ": Connecting to RabbitMQ");
        try {
            this.connection = factory.newConnection();
        } catch (Exception e) {
            throw new ConnectionInitException(e);
        }

        final int poolSize = Integer.parseInt(
                props.getProperty("channelPoolSize", String.valueOf(DEFAULT_CHANNEL_POOL_SIZE)));
        this.channelPool = new ArrayBlockingQueue<>(poolSize);

        try {
            for (int i = 0; i < poolSize; i++) {
                this.channelPool.add(this.connection.createChannel());
            }
        } catch (Exception e) {
            shutdown();
            throw new ConnectionInitException(e);
        }

        logger.info(id + ": RabbitMQ connection established with " + poolSize + " pooled channels");
    }

    @Override
    public void shutdown() {
        if (this.channelPool != null) {
            for (Channel ch : this.channelPool) {
                try {
                    if (ch.isOpen()) ch.close();
                } catch (Exception ignored) {
                }
            }
        }
        if (this.connection != null) {
            try {
                if (this.connection.isOpen()) this.connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public @NotNull com.rabbitmq.client.Connection getConnection() {
        return Objects.requireNonNull(this.connection);
    }

    @Override
    public @NotNull PooledChannel borrowChannel() throws InterruptedException {
        final Channel channel = Objects.requireNonNull(this.channelPool).take();
        return new PooledChannel(channel, this.channelPool);
    }

    @Override
    public boolean isExitOnFailure() {
        return EnvUtils.isExitOnFailure(ConnectionType.RABBITMQ, this.id);
    }
}
