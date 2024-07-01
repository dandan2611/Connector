package fr.codinbox.connector.commons.database;

import fr.codinbox.connector.commons.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface DatabaseConnection {

    @NotNull Connection getConnection();

    void init() throws ConnectionInitException;

    void shutdown();

    boolean isExitOnFailure();

}
