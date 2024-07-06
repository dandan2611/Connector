package fr.codinbox.connector.commons.utils;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EnvUtils {

    public static final @NotNull String REDIS_PREFIX = "CONNECTOR_REDIS_";
    public static final @NotNull String MYSQL_PREFIX = "CONNECTOR_DB_";

    public static @NotNull String getPrefix(final @NotNull ConnectionType connectionType) {
        return switch (connectionType) {
            case REDIS -> REDIS_PREFIX;
            case DATABASE -> MYSQL_PREFIX;
        };
    }

    private static @NotNull String getRedisConnectionPrefix(final @NotNull String id) {
        return REDIS_PREFIX + id.toUpperCase() + "_";
    }

    private static @NotNull String getDatabaseConnectionPrefix(final @NotNull String id) {
        return MYSQL_PREFIX + id.toUpperCase() + "_";
    }

    private static @NotNull String getConnectionPrefix(final @NotNull ConnectionType connectionType,
                                                       final @NotNull String id) {
        return switch (connectionType) {
            case REDIS -> getRedisConnectionPrefix(id);
            case DATABASE -> getDatabaseConnectionPrefix(id);
        };
    }

    public static boolean checkBool(final @Nullable String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
    }

    @CheckReturnValue
    public static boolean isExitOnFailure(final @NotNull ConnectionType connectionType,
                                          final @NotNull String id) {
        return checkBool(System.getenv(getConnectionPrefix(connectionType, id) + "EXIT_ON_FAILURE"));
    }

    @CheckReturnValue
    public static String getConfigFilepath(final @NotNull ConnectionType connectionType,
                                           final @NotNull String id) {
        return System.getenv(getConnectionPrefix(connectionType, id) + "CONFIG");
    }

    @CheckReturnValue
    public static List<String> getEnvironmentIds(final @NotNull ConnectionType connectionType) {
        final String prefix = getPrefix(connectionType);

        return System.getenv().keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .map(key -> key.substring(prefix.length()))
                .map(key -> {
                    int index = key.indexOf('_');
                    if (index == -1) {
                        throw new IllegalArgumentException("Invalid echo environment. Did you specify the connection name?");
                    }
                    return key.substring(0, key.indexOf('_'));
                })
                .distinct()
                .toList();
    }

}
