package app.config.liquibase;

import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfig {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseConfig.class);
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private String[] schemas = {"public", "metadata", "business"};
    private final LiquibaseProperties prop;

    @PostConstruct
    public void initialize() {
        try {
            createSchemas();
            runLiquibase();
            log.info("Migration completed successfully");
        } catch (LiquibaseException e) {
            log.error("Error during Liquibase migration: {}", e.getMessage(), e);
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }

    private void createSchemas() {

        for (String schema : schemas) {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        }
        jdbcTemplate.execute("SET search_path TO metadata, business, public");
        log.debug("Schemas created (if not exist) and search_path set");
    }

    private void runLiquibase() throws LiquibaseException {
        try (var connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(prop.getLiquibaseSchema());

            try (Liquibase liquibase = new Liquibase(prop.getChangeLog(), new ClassLoaderResourceAccessor(), database)) {
                liquibase.update();
            }
        } catch (Exception e) {
            throw new LiquibaseException("Error initializing Liquibase", e);
        }
    }
}
