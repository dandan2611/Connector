package fr.codinbox.redisconnector.connector.redis;

import org.jetbrains.annotations.NotNull;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;

import java.util.Optional;

public interface RedisConnectorService {

    void init() throws ConnectionInitException;

    /**
     * Shutdown the service.
     */
    void shutdown();

    /**
     * Get a connection by id.
     *
     * @param id the id of the connection
     * @return the connection
     */
    @NotNull Optional<RedisConnection> getConnection(final @NotNull String id);

}
