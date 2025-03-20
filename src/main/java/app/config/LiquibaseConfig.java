package app.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class LiquibaseConfig {

    private final Logger log = LoggerFactory.getLogger(LiquibaseConfig.class);
    private final String changeLogFile = "db/changelog/changelog-master.yml";
    private final String liquibaseSchemaName = "metadata";
    private final DbConfig dbConfig;

    public LiquibaseConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void connect() {
        try {
            try (var stmt = dbConfig.getConnection().createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS public;");
                stmt.execute("CREATE SCHEMA IF NOT EXISTS metadata;");
                stmt.execute("CREATE SCHEMA IF NOT EXISTS business;");
                stmt.execute("SET search_path TO metadata, business, public;");
                log.debug("Schemes created (if not exists) and search_path set");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dbConfig.getConnection()));

            Liquibase liquibase =
                    new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);

            database.setLiquibaseSchemaName(liquibaseSchemaName);
            liquibase.update();
            log.info("Migration is completed successfully");
        } catch (LiquibaseException e) {
            log.error("SQL Exception in migration " + e.getMessage());
        }
    }
}