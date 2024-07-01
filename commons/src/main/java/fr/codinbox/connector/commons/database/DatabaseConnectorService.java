package fr.codinbox.connector.commons.database;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface DatabaseConnectorService {

    void init() throws ConnectionInitException;

    /**
     * Shutdown the service.
     */
    void shutdown();

    /**
     * Get a connection by id.
     *
     * @param id the id of the connection
     * @return the connection
     */
    @NotNull Optional<DatabaseConnection> getConnection(final @NotNull String id);

}
