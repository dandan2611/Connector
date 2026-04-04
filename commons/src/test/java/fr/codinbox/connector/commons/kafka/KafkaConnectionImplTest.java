package fr.codinbox.connector.commons.kafka;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConnectionImplTest {

    @TempDir
    Path tempDir;

    private Path writeProperties(String filename, String content) throws Exception {
        Path file = tempDir.resolve(filename);
        try (var writer = new FileWriter(file.toFile())) {
            writer.write(content);
        }
        return file;
    }

    @Test
    void init_withBootstrapServers_succeeds() throws Exception {
        Path propFile = writeProperties("kafka.properties", "bootstrap.servers=localhost:9092\n");
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        conn.init();

        Properties props = conn.getProperties();
        assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
    }

    @Test
    void init_missingBootstrapServers_throws() throws Exception {
        Path propFile = writeProperties("kafka.properties", "key.serializer=org.apache.kafka.common.serialization.StringSerializer\n");
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());

        ConnectionInitException ex = assertThrows(ConnectionInitException.class, conn::init);
        assertTrue(ex.getMessage().contains("bootstrap.servers"));
    }

    @Test
    void init_invalidFilePath_throws() {
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", "/nonexistent/kafka.properties");
        assertThrows(ConnectionInitException.class, conn::init);
    }

    @Test
    void getProperties_returnsDefensiveCopy() throws Exception {
        Path propFile = writeProperties("kafka.properties", "bootstrap.servers=localhost:9092\n");
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        conn.init();

        Properties copy = conn.getProperties();
        copy.put("bootstrap.servers", "modified:9999");

        // Original should be unchanged
        assertEquals("localhost:9092", conn.getProperties().getProperty("bootstrap.servers"));
    }

    @Test
    void init_shutdownTimeoutMs_parsed() throws Exception {
        Path propFile = writeProperties("kafka.properties",
                "bootstrap.servers=localhost:9092\nshutdownTimeoutMs=5000\n");
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        conn.init();

        // shutdownTimeoutMs should be removed from properties
        assertNull(conn.getProperties().getProperty("shutdownTimeoutMs"));
    }

    @Test
    void shutdown_withNoClients_doesNotThrow() throws Exception {
        Path propFile = writeProperties("kafka.properties", "bootstrap.servers=localhost:9092\n");
        var conn = new KafkaConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        conn.init();
        assertDoesNotThrow(conn::shutdown);
    }
}
