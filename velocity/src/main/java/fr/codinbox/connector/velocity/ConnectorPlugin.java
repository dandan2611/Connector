package fr.codinbox.connector.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.codinbox.connector.commons.database.DatabaseConnectorServiceImpl;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.redis.RedisConnectorServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Plugin(
        id = "connector",
        name = "connector",
        version = "6.0.6",
        authors = {"dandan2611"}
)
public class ConnectorPlugin {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    public ConnectorPlugin() {
        final java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("connector");

        final var databaseServiceImpl = new DatabaseConnectorServiceImpl(javaLogger);

        try {
            databaseServiceImpl.init();
            Connector.setDatabaseService(databaseServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.error("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }

        final var redisServiceImpl = new RedisConnectorServiceImpl(javaLogger);

        try {
            redisServiceImpl.init();
            Connector.setRedisService(redisServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.error("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }
    }

    @Subscribe(order = PostOrder.LAST, async = false)
    private void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
        Connector.getDatabaseService().shutdown();
        Connector.getRedisService().shutdown();
    }

}
