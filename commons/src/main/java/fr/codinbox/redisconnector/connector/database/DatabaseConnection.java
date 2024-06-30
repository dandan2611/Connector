package fr.codinbox.redisconnector.connector.database;

import fr.codinbox.redisconnector.connector.exception.ConnectionInitException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface DatabaseConnection {

    @NotNull Connection getConnection();

    void init() throws ConnectionInitException;

    void shutdown();

    boolean isExitOnFailure();

}
