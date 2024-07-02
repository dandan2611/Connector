package fr.codinbox.connector.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.redis.RedisConnectorServiceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Plugin(
        id = "connector",
        name = "connector",
        version = "6.0.2",
        authors = {"dandan2611"}
)
public class ConnectorPlugin {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    public ConnectorPlugin() {
        final var databaseServiceImpl = new RedisConnectorServiceImpl(this.logger);

        try {
            databaseServiceImpl.init();
            Connector.setRedisService(databaseServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }

        final var redisServiceImpl = new RedisConnectorServiceImpl(this.logger);

        try {
            redisServiceImpl.init();
            Connector.setRedisService(redisServiceImpl);
        } catch (ConnectionInitException exception) {
            this.logger.severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            this.server.shutdown();
            return;
        }
    }

    @Subscribe
    private void onProxyShutdown(final @NotNull ProxyShutdownEvent event) {
        Connector.getDatabaseService().shutdown();
        Connector.getRedisService().shutdown();
    }

}
