package fr.codinbox.connector.commons.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class EnvUtilsTest {

    @SystemStub
    private EnvironmentVariables env;

    @Test
    void checkBool_trueValues() {
        assertTrue(EnvUtils.checkBool("true"));
        assertTrue(EnvUtils.checkBool("TRUE"));
        assertTrue(EnvUtils.checkBool("True"));
        assertTrue(EnvUtils.checkBool("1"));
    }

    @Test
    void checkBool_falseValues() {
        assertFalse(EnvUtils.checkBool("false"));
        assertFalse(EnvUtils.checkBool("0"));
        assertFalse(EnvUtils.checkBool(""));
        assertFalse(EnvUtils.checkBool(null));
        assertFalse(EnvUtils.checkBool("yes"));
    }

    @Test
    void getPrefix_allTypes() {
        assertEquals("CONNECTOR_REDIS_", EnvUtils.getPrefix(ConnectionType.REDIS));
        assertEquals("CONNECTOR_DB_", EnvUtils.getPrefix(ConnectionType.DATABASE));
        assertEquals("CONNECTOR_RABBITMQ_", EnvUtils.getPrefix(ConnectionType.RABBITMQ));
        assertEquals("CONNECTOR_KAFKA_", EnvUtils.getPrefix(ConnectionType.KAFKA));
    }

    @Test
    void isExitOnFailure_returnsTrue() {
        env.set("CONNECTOR_REDIS_MAIN_EXIT_ON_FAILURE", "true");
        assertTrue(EnvUtils.isExitOnFailure(ConnectionType.REDIS, "MAIN"));
    }

    @Test
    void isExitOnFailure_returnsFalse() {
        env.set("CONNECTOR_REDIS_MAIN_EXIT_ON_FAILURE", "false");
        assertFalse(EnvUtils.isExitOnFailure(ConnectionType.REDIS, "MAIN"));
    }

    @Test
    void isExitOnFailure_notSet() {
        assertFalse(EnvUtils.isExitOnFailure(ConnectionType.REDIS, "MISSING"));
    }

    @Test
    void getConfigFilepath_returnsPath() {
        env.set("CONNECTOR_DB_PROD_CONFIG", "/etc/db.properties");
        assertEquals("/etc/db.properties", EnvUtils.getConfigFilepath(ConnectionType.DATABASE, "PROD"));
    }

    @Test
    void getConfigFilepath_returnsNull() {
        assertNull(EnvUtils.getConfigFilepath(ConnectionType.DATABASE, "MISSING"));
    }

    @Test
    void getEnvironmentIds_discoversIds() {
        env.set("CONNECTOR_REDIS_MAIN_CONFIG", "/path/main.yml");
        env.set("CONNECTOR_REDIS_MAIN_EXIT_ON_FAILURE", "true");
        env.set("CONNECTOR_REDIS_CACHE_CONFIG", "/path/cache.yml");

        List<String> ids = EnvUtils.getEnvironmentIds(ConnectionType.REDIS);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("MAIN"));
        assertTrue(ids.contains("CACHE"));
    }

    @Test
    void getEnvironmentIds_empty() {
        List<String> ids = EnvUtils.getEnvironmentIds(ConnectionType.KAFKA);
        assertTrue(ids.isEmpty());
    }

    @Test
    void getEnvironmentIds_deduplicates() {
        env.set("CONNECTOR_RABBITMQ_TEST_CONFIG", "/path/config.properties");
        env.set("CONNECTOR_RABBITMQ_TEST_EXIT_ON_FAILURE", "false");

        List<String> ids = EnvUtils.getEnvironmentIds(ConnectionType.RABBITMQ);
        assertEquals(1, ids.size());
        assertEquals("TEST", ids.get(0));
    }

    @Test
    void getEnvironmentIds_kafkaPrefix() {
        env.set("CONNECTOR_KAFKA_EVENTS_CONFIG", "/path/kafka.properties");

        List<String> ids = EnvUtils.getEnvironmentIds(ConnectionType.KAFKA);
        assertEquals(1, ids.size());
        assertEquals("EVENTS", ids.get(0));
    }
}
