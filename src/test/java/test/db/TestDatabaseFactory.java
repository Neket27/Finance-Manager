package test.db;


import app.util.ConfigLoader;
import test.config.AppProperties;
import test.db.config.LiquibaseConfig;
import test.db.config.PostgresContainer;

import java.sql.Connection;

public class TestDatabaseFactory {

    private static PostgresContainer container;
    public static AppProperties appProperties;

    public static TestDatabase create() {
        if (container == null) {
            appProperties = ConfigLoader.loadConfig("application-test.yml", AppProperties.class);
            container = new PostgresContainer(appProperties.getPostgresContainer());
            container.startContainer();
        }

        Connection connection = container.getConnection();

        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(connection,appProperties.getLiquibase());
        liquibaseConfig.initializeAndMigrate();

        return new TestDatabase(connection, container, liquibaseConfig);
    }

    public static void reset(){
        container.stopContainer();
        container = null;
    }

}

