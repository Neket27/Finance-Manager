package app.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static app.config.DbConfig.*;

public class LiquibaseConfig {

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase =
                    new Liquibase("db/changelog/changelog-master.yml", new ClassLoaderResourceAccessor(), database);

            database.setLiquibaseSchemaName("metadata");
            liquibase.update();
            System.out.println("Migration is completed successfully");
        } catch (SQLException | LiquibaseException e) {
            System.out.println("SQL Exception in migration " + e.getMessage());
        }
    }
}