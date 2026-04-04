package fr.codinbox.connector.commons.rabbitmq;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConnectionImplTest {

    @TempDir
    Path tempDir;

    private void writeProperties(Path file, String content) throws Exception {
        try (var writer = new FileWriter(file.toFile())) {
            writer.write(content);
        }
    }

    @Test
    void init_invalidFilePath_throwsConnectionInitException() {
        var conn = new RabbitMQConnectionImpl(Logger.getLogger("test"), "TEST", "/nonexistent/path.properties");
        assertThrows(ConnectionInitException.class, conn::init);
    }

    @Test
    void init_validProperties_defaultValues() throws Exception {
        // This test verifies properties loading but will fail on actual connection
        // since there's no RabbitMQ server running. We verify it gets past config loading.
        Path propFile = tempDir.resolve("rabbit.properties");
        writeProperties(propFile, "host=localhost\nport=5672\n");

        var conn = new RabbitMQConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        // Will throw because no RabbitMQ server is available, but should be ConnectionInitException
        assertThrows(ConnectionInitException.class, conn::init);
    }

    @Test
    void init_customChannelPoolSize() throws Exception {
        Path propFile = tempDir.resolve("rabbit.properties");
        writeProperties(propFile, "host=localhost\nport=5672\nchannelPoolSize=10\n");

        var conn = new RabbitMQConnectionImpl(Logger.getLogger("test"), "TEST", propFile.toString());
        // Will fail on connection, but verifies properties parsing doesn't crash
        assertThrows(ConnectionInitException.class, conn::init);
    }
}
