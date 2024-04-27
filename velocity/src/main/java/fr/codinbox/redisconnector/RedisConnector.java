package fr.codinbox.redisconnector;

import fr.codinbox.redisconnector.connector.RedisConnectorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RedisConnector {

    private static @Nullable RedisConnectorService redisConnectorService = null;

    public static @NotNull RedisConnectorService getService() {
        return Objects.requireNonNull(redisConnectorService, "RedisConnectorService has not been initialized");
    }

    protected static void setService(final @NotNull RedisConnectorService service) {
        redisConnectorService = service;
    }

}
