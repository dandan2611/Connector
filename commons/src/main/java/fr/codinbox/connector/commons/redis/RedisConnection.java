package fr.codinbox.connector.commons.redis;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

/**
 * Represents a managed Redis connection backed by Redisson.
 *
 * <p>Connections are configured via YAML files ({@code .yml} or {@code .yaml}) referenced
 * by the {@code CONNECTOR_REDIS_<ID>_CONFIG} environment variable. The YAML format
 * follows the Redisson configuration specification.</p>
 *
 * @see RedisConnectorService
 * @see fr.codinbox.connector.commons.codec.JsonJacksonConnectorCodec
 */
public interface RedisConnection {

    /**
     * Returns the underlying Redisson client.
     *
     * @return the {@link RedissonClient} instance
     * @throws NullPointerException if called before {@link #init()}
     */
    @NotNull RedissonClient getClient();

    /**
     * Initializes the Redis connection by loading configuration and creating the Redisson client.
     *
     * @throws ConnectionInitException if the configuration file is invalid or the connection fails
     */
    void init() throws ConnectionInitException;

    /**
     * Shuts down the Redisson client and releases all associated resources.
     */
    void shutdown();

    /**
     * Returns whether this connection is configured to cause server shutdown on initialization failure.
     *
     * @return {@code true} if the server should exit when this connection fails to initialize
     */
    boolean isExitOnFailure();
}
