package fr.codinbox.redisconnector.connector.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;
import fr.codinbox.redisconnector.utils.ConnectionType;
import fr.codinbox.redisconnector.utils.EnvUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnectionImpl implements DatabaseConnection {

    private final @NotNull Logger logger;

    private final @NotNull String id;

    private final @NotNull String configFilePath;

    private @Nullable HikariDataSource dataSource;

    public DatabaseConnectionImpl(final @NotNull Logger logger,
                                  final @NotNull String id,
                                  final @NotNull String configFilePath) {
        this.logger = logger;
        this.id = id;
        this.configFilePath = configFilePath;
    }

    @Override
    public void init() throws ConnectionInitException {
        final HikariConfig config = new HikariConfig(this.configFilePath);

        logger.fine(id + ": Connecting to database connection");
        try {
            this.dataSource = new HikariDataSource(config);
        } catch (Exception exception) {
            throw new ConnectionInitException(exception);
        }
    }

    @Override
    public void shutdown() {
        if (this.dataSource != null)
            this.dataSource.close();
    }

    @Override
    public @NotNull Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isExitOnFailure() {
        return EnvUtils.isExitOnFailure(ConnectionType.DATABASE, this.id);
    }

}
