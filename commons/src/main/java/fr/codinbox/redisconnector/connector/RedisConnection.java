package fr.codinbox.redisconnector.connector;

import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;

public interface RedisConnection {

    @NotNull RedissonClient getClient();

    void init() throws ConnectionInitException;

    void shutdown();

    boolean isExitOnFailure();

}
