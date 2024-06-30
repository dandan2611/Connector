package fr.codinbox.redisconnector;

import fr.codinbox.redisconnector.connector.database.DatabaseConnectorService;
import fr.codinbox.redisconnector.connector.redis.RedisConnectorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Connector {

    private static @Nullable DatabaseConnectorService databaseConnectorService = null;
    private static @Nullable RedisConnectorService redisConnectorService = null;

    public static @NotNull DatabaseConnectorService getDatabaseService() {
        return Objects.requireNonNull(databaseConnectorService, "DatabaseConnectorService has not been initialized");
    }

    public static @NotNull RedisConnectorService getRedisService() {
        return Objects.requireNonNull(redisConnectorService, "RedisConnectorService has not been initialized");
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

}
