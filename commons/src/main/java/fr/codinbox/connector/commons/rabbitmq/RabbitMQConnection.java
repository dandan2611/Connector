package fr.codinbox.connector.commons.rabbitmq;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a managed RabbitMQ connection with channel pooling support.
 *
 * <p>Connections are configured via {@code .properties} files referenced by the
 * {@code CONNECTOR_RABBITMQ_<ID>_CONFIG} environment variable. Supported properties:</p>
 * <ul>
 *   <li>{@code host} — broker hostname (default: {@code localhost})</li>
 *   <li>{@code port} — broker port (default: {@code 5672})</li>
 *   <li>{@code username} — authentication username (default: {@code guest})</li>
 *   <li>{@code password} — authentication password (default: {@code guest})</li>
 *   <li>{@code virtualHost} — virtual host (default: {@code /})</li>
 *   <li>{@code ssl} — enable SSL with default JVM SSLContext (default: {@code false})</li>
 *   <li>{@code channelPoolSize} — fixed channel pool size (default: {@code 5})</li>
 * </ul>
 *
 * <p>Channels are managed through a fixed-size pool. Use {@link #borrowChannel()} to obtain
 * a {@link PooledChannel} that automatically returns to the pool when closed:</p>
 * <pre>{@code
 * try (var channel = connection.borrowChannel()) {
 *     channel.basicPublish("exchange", "key", null, "message".getBytes());
 * }
 * }</pre>
 *
 * @see RabbitMQConnectorService
 * @see PooledChannel
 */
public interface RabbitMQConnection {

    /**
     * Returns the underlying RabbitMQ connection.
     *
     * @return the raw {@link com.rabbitmq.client.Connection} instance
     * @throws NullPointerException if called before {@link #init()}
     */
    @NotNull com.rabbitmq.client.Connection getConnection();

    /**
     * Borrows a channel from the fixed-size pool.
     *
     * <p>The returned {@link PooledChannel} implements {@link AutoCloseable} and should be
     * used in a try-with-resources block to ensure the channel is returned to the pool.</p>
     *
     * @return a pooled channel wrapper
     * @throws InterruptedException if the current thread is interrupted while waiting for a channel
     */
    @NotNull PooledChannel borrowChannel() throws InterruptedException;

    /**
     * Initializes the RabbitMQ connection and pre-creates the channel pool.
     *
     * @throws ConnectionInitException if the connection or channel pool creation fails
     */
    void init() throws ConnectionInitException;

    /**
     * Shuts down the connection, closing all pooled channels and the underlying connection.
     */
    void shutdown();

    /**
     * Returns whether this connection is configured to cause server shutdown on initialization failure.
     *
     * @return {@code true} if the server should exit when this connection fails to initialize
     */
    boolean isExitOnFailure();
}
