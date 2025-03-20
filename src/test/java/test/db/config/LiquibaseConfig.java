package test.db.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class LiquibaseConfig {

    private final Logger log = LoggerFactory.getLogger(app.config.LiquibaseConfig.class);
    private final String changeLogFile = "db/test-changelog/changelog-master.yml";
    private final String liquibaseSchemaName = "metadata";
    private final Connection connection;

    public LiquibaseConfig(Connection connection) {
        this.connection = connection;
    }

    public void initializeAndMigrate() {
        createSchemasAndSetSearchPath();
        runLiquibaseMigrations();
    }

    private void createSchemasAndSetSearchPath() {
        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS public;");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS metadata;");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS business;");
            stmt.execute("SET search_path TO metadata, business, public;");

            try (var rs = stmt.executeQuery("SHOW search_path;")) {
                if (rs.next()) {
                    log.debug("Current search_path: {}", rs.getString(1));
                }
            }

            log.info("Schemas created and search_path configured");
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating schemas or setting search_path", e);
        }
    }

    private void runLiquibaseMigrations() {
        try {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setLiquibaseSchemaName(liquibaseSchemaName);

            Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
            liquibase.update();
            log.info("Liquibase migration completed successfully");
        } catch (LiquibaseException e) {
            throw new RuntimeException("Error running Liquibase migrations", e);
        }
    }

    public void resetAndMigrate() {
        cleanLiquibaseMetadata();
        runLiquibaseMigrations();
    }

    private void cleanLiquibaseMetadata() {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS metadata.databasechangelog CASCADE;");
            stmt.execute("DROP TABLE IF EXISTS metadata.databasechangeloglock CASCADE;");
            log.info("Liquibase metadata tables dropped, ready for fresh migration");
        } catch (SQLException e) {
            throw new RuntimeException("Error while dropping Liquibase metadata tables", e);
        }
    }

}
