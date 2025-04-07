package test.integration.db.config;

import app.config.LiquibaseConfigProperties;
import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
@Configuration
@Profile("test")
@RequiredArgsConstructor
@EnableConfigurationProperties({LiquibaseConfigProperties.class, LiquibaseProperties.class})
public class LiquibaseConfig {

    private final JdbcTemplate jdbcTemplate;
    private final LiquibaseProperties prop;
    private final LiquibaseConfigProperties liquibaseConfigProperties;

    @PostConstruct
    public void initialize() {
        createSchemas();
        runLiquibase();
        log.info("Migration completed successfully");
    }

    private void createSchemas() {

        for (String schema : liquibaseConfigProperties.getCreateSchemaLocations()) {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        }
        jdbcTemplate.execute("SET search_path TO metadata, business, public");
        log.debug("Schemas created (if not exist) and search_path set");
    }

    private void runLiquibase() {
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(prop.getLiquibaseSchema());

            try (Liquibase liquibase = new Liquibase(prop.getChangeLog(), new ClassLoaderResourceAccessor(), database)) {
                liquibase.update();
            }
        } catch (Exception e) {
            log.error("Error initializing Liquibase", e);
        }
    }


    public void dropSchemas() {
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
            var statement = connection.createStatement();
            List<String> schemas = List.of("public", "metadata", "business");

            for (String schema : schemas)
                statement.execute("DROP SCHEMA " + schema + " CASCADE;");


        } catch (Exception e) {
            log.error("Error drop schemas" + e);
        }
    }

}