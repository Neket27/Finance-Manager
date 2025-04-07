package test.integration.db.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class LiquibaseConfig {

    private final Logger log = LoggerFactory.getLogger(LiquibaseConfig.class);
    private final JdbcTemplate jdbcTemplate;
    private final AppProperties.LiquibaseProperties prop;
    private final DriverManagerDataSource dataSource;

    public LiquibaseConfig(JdbcTemplate jdbcTemplate, DriverManagerDataSource dataSource, AppProperties.LiquibaseProperties liquibaseProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.prop = liquibaseProperties;
    }

    public void initializeAndMigrate() {
        createSchemasAndSetSearchPath();
        runLiquibaseMigrations();
    }

    private void createSchemasAndSetSearchPath() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS public;");
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS metadata;");
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS business;");
        jdbcTemplate.execute("SET search_path TO metadata, business, public;");

        String searchPath = jdbcTemplate.queryForObject("SHOW search_path;", String.class);
        log.debug("Current search_path: {}", searchPath);
        log.info("Schemas created and search_path configured");
    }

    private void runLiquibaseMigrations() {
        try (var connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(prop.getLiquibaseSchemaName());

            Liquibase liquibase = new Liquibase(prop.getChangeLogFile(), new ClassLoaderResourceAccessor(), database);
            liquibase.update();
            log.info("Liquibase migration completed successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error running Liquibase migrations", e);
        }
    }

    public void resetAndMigrate() {
        cleanLiquibaseMetadata();
        runLiquibaseMigrations();
    }

    private void cleanLiquibaseMetadata() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS metadata.databasechangelog CASCADE;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS metadata.databasechangeloglock CASCADE;");
        log.info("Liquibase metadata tables dropped, ready for fresh migration");
    }
}
