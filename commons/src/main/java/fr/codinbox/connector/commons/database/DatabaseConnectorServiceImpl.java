package fr.codinbox.connector.commons.database;

import fr.codinbox.connector.commons.utils.ConnectionType;
import fr.codinbox.connector.commons.exception.ConnectionInitException;
import fr.codinbox.connector.commons.utils.EnvUtils;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@link DatabaseConnectorService} that discovers and manages
 * database connections from environment variables.
 *
 * @see DatabaseConnectorService
 * @see DatabaseConnectionImpl
 */
public class DatabaseConnectorServiceImpl implements DatabaseConnectorService {

    private static final @NotNull ConnectionType CONNECTION_TYPE = ConnectionType.DATABASE;

    private final @NotNull Logger logger;

    private final @NotNull Map<String, DatabaseConnection> sqlConnectionMap;

    /**
     * Creates a new database connector service.
     *
     * @param logger the logger for diagnostic messages
     */
    public DatabaseConnectorServiceImpl(final @NotNull Logger logger) {
        this.logger = logger;
        this.sqlConnectionMap = new HashMap<>();
    }

    @Override
    public void init() throws ConnectionInitException {
        // Connect to db servers
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        final var idList = EnvUtils.getEnvironmentIds(CONNECTION_TYPE);
        this.logger.info(String.format("%d connection(s) to initialize", idList.size()));
        for (String id : idList) {
            final var configFilepath = EnvUtils.getConfigFilepath(CONNECTION_TYPE, id);

            if (configFilepath == null) {
                logger.warning("No configuration file found for id: " + id);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("No configuration file found for id: " + id);
                }
                continue;
            }

            try {
                this.logger.info(id + ": Creating connection object");
                final var connection = new DatabaseConnectionImpl(this.logger, id, configFilepath);
                connection.init();
                this.sqlConnectionMap.put(id, connection);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to create connection for id: " + id, exception);
                if (EnvUtils.isExitOnFailure(CONNECTION_TYPE, id)) {
                    throw new ConnectionInitException("Failed to create connection for id: " + id);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        for (DatabaseConnection value : this.sqlConnectionMap.values())
            value.shutdown();
    }

    @Override
    public @NotNull Optional<DatabaseConnection> getConnection(@NotNull String id) {
        return Optional.ofNullable(this.sqlConnectionMap.get(id));
    }

}
