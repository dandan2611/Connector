package fr.codinbox.redisconnector.utils;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EnvUtils {

    private static @NotNull String getConnectionPrefix(final @NotNull String id) {
        return "CONNECTOR_REDIS_" + id.toUpperCase() + "_";
    }

    private static boolean checkBool(final String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
    }

    @CheckReturnValue
    public static boolean isExitOnFailure(final String id) {
        return checkBool(System.getenv(getConnectionPrefix(id) + "EXIT_ON_FAILURE"));
    }

    @CheckReturnValue
    public static String getConfigFilepath(final String id) {
        return System.getenv(getConnectionPrefix(id) + "CONFIG");
    }

    @CheckReturnValue
    public static List<String> getEnvironmentIds() {
        return System.getenv().keySet().stream()
                .filter(key -> key.startsWith("CONNECTOR_REDIS_"))
                .map(key -> key.substring("CONNECTOR_REDIS_".length()))
                .map(key -> key.substring(0, key.indexOf('_')))
                .distinct()
                .toList();
    }

}
