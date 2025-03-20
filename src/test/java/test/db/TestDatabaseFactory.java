package test.db;

import test.db.config.LiquibaseConfig;
import test.db.config.PostgresContainer;

import java.sql.Connection;

public class TestDatabaseFactory {

    private static PostgresContainer container;

    public static TestDatabase create() {
        if (container == null) {
            container = new PostgresContainer();
            container.startContainer();
        }

        Connection connection = container.getConnection();

        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(connection);
        liquibaseConfig.initializeAndMigrate();

        return new TestDatabase(connection, container, liquibaseConfig);
    }

    public static void reset(){
        container.stopContainer();
        container = null;
    }

}

