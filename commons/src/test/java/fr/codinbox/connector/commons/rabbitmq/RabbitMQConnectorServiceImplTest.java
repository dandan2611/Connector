package fr.codinbox.connector.commons.rabbitmq;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class RabbitMQConnectorServiceImplTest {

    @SystemStub
    private EnvironmentVariables env;

    private RabbitMQConnectorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RabbitMQConnectorServiceImpl(Logger.getLogger("test"));
    }

    @Test
    void init_noConnections() throws ConnectionInitException {
        service.init();
        assertEquals(Optional.empty(), service.getConnection("ANYTHING"));
    }

    @Test
    void getConnection_unknownId_returnsEmpty() throws ConnectionInitException {
        service.init();
        assertTrue(service.getConnection("NONEXISTENT").isEmpty());
    }

    @Test
    void init_missingConfig_exitOnFailure_throws() {
        env.set("CONNECTOR_RABBITMQ_BAD_EXIT_ON_FAILURE", "true");
        env.set("CONNECTOR_RABBITMQ_BAD_OTHER", "value");

        assertThrows(ConnectionInitException.class, () -> service.init());
    }

    @Test
    void init_missingConfig_noExitOnFailure_skips() throws ConnectionInitException {
        env.set("CONNECTOR_RABBITMQ_BAD_EXIT_ON_FAILURE", "false");
        env.set("CONNECTOR_RABBITMQ_BAD_OTHER", "value");

        service.init();
        assertTrue(service.getConnection("BAD").isEmpty());
    }
}
