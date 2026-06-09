package fr.codinbox.connector.commons.utils;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utility class for reading connector configuration from environment variables.
 *
 * <p>Each connector type uses a specific prefix for its environment variables:
 * <ul>
 *   <li>Redis: {@value #REDIS_PREFIX}</li>
 *   <li>Database: {@value #MYSQL_PREFIX}</li>
 *   <li>RabbitMQ: {@value #RABBITMQ_PREFIX}</li>
 * </ul>
 *
 * <p>Environment variables follow the pattern
 * {@code <PREFIX><CONNECTION_ID>_<PROPERTY>}, for example
 * {@code CONNECTOR_REDIS_MAIN_CONFIG=/path/to/config.yml}.</p>
 *
 * @see ConnectionType
 */
public final class EnvUtils {

    /**
     * Environment variable prefix for Redis connections.
     */
    public static final @NotNull String REDIS_PREFIX = "CONNECTOR_REDIS_";

    /**
     * Environment variable prefix for database connections.
     */
    public static final @NotNull String MYSQL_PREFIX = "CONNECTOR_DB_";

    /**
     * Environment variable prefix for RabbitMQ connections.
     */
    public static final @NotNull String RABBITMQ_PREFIX = "CONNECTOR_RABBITMQ_";

    /**
     * Returns the environment variable prefix for the given connection type.
     *
     * @param connectionType the connection type
     * @return the prefix string used in environment variable names
     */
    public static @NotNull String getPrefix(final @NotNull ConnectionType connectionType) {
        return switch (connectionType) {
            case REDIS -> REDIS_PREFIX;
            case DATABASE -> MYSQL_PREFIX;
            case RABBITMQ -> RABBITMQ_PREFIX;
        };
    }

    private static @NotNull String getRedisConnectionPrefix(final @NotNull String id) {
        return REDIS_PREFIX + id.toUpperCase() + "_";
    }

    private static @NotNull String getDatabaseConnectionPrefix(final @NotNull String id) {
        return MYSQL_PREFIX + id.toUpperCase() + "_";
    }

    private static @NotNull String getRabbitMQConnectionPrefix(final @NotNull String id) {
        return RABBITMQ_PREFIX + id.toUpperCase() + "_";
    }

    private static @NotNull String getConnectionPrefix(final @NotNull ConnectionType connectionType,
                                                       final @NotNull String id) {
        return switch (connectionType) {
            case REDIS -> getRedisConnectionPrefix(id);
            case DATABASE -> getDatabaseConnectionPrefix(id);
            case RABBITMQ -> getRabbitMQConnectionPrefix(id);
        };
    }

    /**
     * Checks whether the given string represents a boolean {@code true} value.
     *
     * <p>Recognized true values are {@code "true"} and {@code "1"} (case-insensitive).
     * All other values, including {@code null}, return {@code false}.</p>
     *
     * @param value the string to check, may be {@code null}
     * @return {@code true} if the value represents a boolean true
     */
    public static boolean checkBool(final @Nullable String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
    }

    /**
     * Determines whether the given connection should cause the server to exit on initialization failure.
     *
     * <p>Reads the environment variable {@code <PREFIX><ID>_EXIT_ON_FAILURE}.</p>
     *
     * @param connectionType the connection type
     * @param id             the connection identifier
     * @return {@code true} if the connection is configured to exit on failure
     */
    @CheckReturnValue
    public static boolean isExitOnFailure(final @NotNull ConnectionType connectionType,
                                          final @NotNull String id) {
        return checkBool(System.getenv(getConnectionPrefix(connectionType, id) + "EXIT_ON_FAILURE"));
    }

    /**
     * Retrieves the configuration file path for the given connection from environment variables.
     *
     * <p>Reads the environment variable {@code <PREFIX><ID>_CONFIG}.</p>
     *
     * @param connectionType the connection type
     * @param id             the connection identifier
     * @return the configuration file path, or {@code null} if not set
     */
    @CheckReturnValue
    public static String getConfigFilepath(final @NotNull ConnectionType connectionType,
                                           final @NotNull String id) {
        return System.getenv(getConnectionPrefix(connectionType, id) + "CONFIG");
    }

    /**
     * Discovers all configured connection identifiers for the given connection type
     * by scanning environment variable names.
     *
     * <p>For example, if the environment contains {@code CONNECTOR_REDIS_MAIN_CONFIG}
     * and {@code CONNECTOR_REDIS_MAIN_EXIT_ON_FAILURE}, this method returns {@code ["MAIN"]}.</p>
     *
     * @param connectionType the connection type to scan for
     * @return a list of distinct connection identifiers
     * @throws IllegalArgumentException if an environment variable has an invalid format
     */
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
