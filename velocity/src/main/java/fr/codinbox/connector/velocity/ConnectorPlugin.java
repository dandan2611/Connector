package fr.codinbox.connector.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.codinbox.connector.commons.database.DatabaseConnectorServiceImpl;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.rabbitmq.RabbitMQConnectorServiceImpl;
import fr.codinbox.connector.commons.redis.RedisConnectorServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Velocity proxy plugin entry point for the Connector library.
 *
 * <p>This plugin initializes all three connector services (Database, Redis, RabbitMQ)
 * and makes them available through the {@link Connector} static accessor.</p>
 *
 * <p>Initialization order: Database, Redis, RabbitMQ. If any connector with
 * exit-on-failure enabled fails, the proxy is shut down immediately.</p>
 *
 * @see Connector
 */
@Plugin(
        id = "connector",
        name = "connector",
        version = "8.0.0",
        authors = {"dandan2611"}
)
public class ConnectorPlugin {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    public ConnectorPlugin() {
        final java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("connector");

        // Database
        final var databaseServiceImpl = new DatabaseConnectorServiceImpl(javaLogger);
        try {
            databaseServiceImpl.init();
            Connector.setDatabaseService(databaseServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.error("Failed to initialize DatabaseConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }

        // Redis
        final var redisServiceImpl = new RedisConnectorServiceImpl(javaLogger);
        try {
            redisServiceImpl.init();
            Connector.setRedisService(redisServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.error("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }

        // RabbitMQ
        final var rabbitMQServiceImpl = new RabbitMQConnectorServiceImpl(javaLogger);
        try {
            rabbitMQServiceImpl.init();
            Connector.setRabbitMQService(rabbitMQServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.error("Failed to initialize RabbitMQConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }
    }

    @Subscribe(order = PostOrder.LAST, async = false)
    private void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
        Connector.getRabbitMQService().shutdown();
        Connector.getRedisService().shutdown();
        Connector.getDatabaseService().shutdown();
    }
}
