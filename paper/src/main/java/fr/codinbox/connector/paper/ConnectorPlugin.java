package fr.codinbox.connector.paper;

import fr.codinbox.connector.commons.database.DatabaseConnectorService;
import fr.codinbox.connector.commons.database.DatabaseConnectorServiceImpl;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.redis.RedisConnectorService;
import fr.codinbox.connector.commons.redis.RedisConnectorServiceImpl;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class ConnectorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final var databaseServiceImpl = new DatabaseConnectorServiceImpl(super.getLogger());

        try {
            databaseServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize DatabaseConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }

        super.getServer().getServicesManager().register(
                DatabaseConnectorService.class,
                databaseServiceImpl,
                this,
                ServicePriority.Normal
        );

        final var redisServiceImpl = new RedisConnectorServiceImpl(super.getLogger());

        try {
            redisServiceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }

        super.getServer().getServicesManager().register(
                RedisConnectorService.class,
                redisServiceImpl,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        final var redisService = super.getServer().getServicesManager().load(RedisConnectorService.class);
        if (redisService != null) {
            redisService.shutdown();
        }

        final var databaseService = super.getServer().getServicesManager().load(DatabaseConnectorService.class);
        if (databaseService != null) {
            databaseService.shutdown();
        }

        super.getServer().getServicesManager().unregisterAll(this);
    }

}
