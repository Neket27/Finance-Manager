package test.integration.db;


import org.springframework.jdbc.core.JdbcTemplate;
import test.integration.db.config.LiquibaseConfig;
import test.integration.db.config.PostgresContainer;

public record TestDatabase(JdbcTemplate jdbcTemplate, PostgresContainer container, LiquibaseConfig liquibaseConfig) {

    public void stop() {
        container.stopContainer();
    }

    public void clear() {
//        container.clear();
        liquibaseConfig.resetAndMigrate();

    }


}

