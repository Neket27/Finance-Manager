package app.config;

import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LiquibaseConfig {

    private final JdbcTemplate jdbcTemplate;
    private final LiquibaseProperties prop;

    private String[] schemas = {"public", "metadata", "business"};

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
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
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