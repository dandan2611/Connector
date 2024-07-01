package fr.codinbox.connector.commons.redis;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

public interface RedisConnection {

    @NotNull RedissonClient getClient();

    void init() throws ConnectionInitException;

    void shutdown();

    boolean isExitOnFailure();

}
