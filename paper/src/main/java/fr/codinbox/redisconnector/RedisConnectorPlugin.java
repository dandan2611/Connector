package fr.codinbox.redisconnector;

import fr.codinbox.redisconnector.connector.redis.RedisConnectorService;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import fr.codinbox.redisconnector.connector.redis.RedisConnectorServiceImpl;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;

public class RedisConnectorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final var serviceImpl = new RedisConnectorServiceImpl(super.getLogger());

        try {
            serviceImpl.init();
        } catch (ConnectionInitException exception) {
            super.getLogger().severe("Failed to initialize RedisConnectorService, one or more connections failed to initialize and has exit on failure enabled. Shutting down server.");
            super.getServer().shutdown();
            return;
        }

        super.getServer().getServicesManager().register(
                RedisConnectorService.class,
                serviceImpl,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        final var service = super.getServer().getServicesManager().load(RedisConnectorService.class);

        if (service != null) {
            service.shutdown();
            super.getServer().getServicesManager().unregisterAll(this);
        }
    }

}
