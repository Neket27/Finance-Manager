package test.db;

import test.db.config.LiquibaseConfig;
import test.db.config.PostgresContainer;

import java.sql.Connection;

public record TestDatabase(Connection connection, PostgresContainer container, LiquibaseConfig liquibaseConfig) {

    public void stop() {
        container.stopContainer();
    }

    public void clear() {
        container.clear();
        liquibaseConfig.resetAndMigrate();

    }

}

