package test.integration.db;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import test.integration.utils.ConfigLoader;
import test.integration.db.config.AppProperties;
import test.integration.db.config.LiquibaseConfig;
import test.integration.db.config.PostgresContainer;

public class TestDatabaseFactory {

    private static PostgresContainer container;
    public static AppProperties appProperties;

    public static TestDatabase create() {
        if (container == null) {
            appProperties = ConfigLoader.loadConfig("application-test.yml", AppProperties.class);
            container = new PostgresContainer(appProperties.getPostgresContainer());
            container.startContainer();
        }

        DriverManagerDataSource dataSource = container.driverManagerDataSource();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(jdbcTemplate, dataSource, appProperties.getLiquibase());
        liquibaseConfig.initializeAndMigrate();

        return new TestDatabase(jdbcTemplate, container, liquibaseConfig);
    }

    public static void reset() {
        container.stopContainer();
        container = null;
    }

}

