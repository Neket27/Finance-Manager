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
    private DbConfig dbConfig;
    private AppProperties.LiquibaseProperties prop;

    public LiquibaseConfig(DbConfig dbConfig, AppProperties.LiquibaseProperties prop) {
        this.dbConfig = dbConfig;
        this.prop = prop;
    }

    public Logger getLog() {
        return log;
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public AppProperties.LiquibaseProperties getProp() {
        return prop;
    }

    public void setProp(AppProperties.LiquibaseProperties prop) {
        this.prop = prop;
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

            Liquibase _liquibase =
                    new Liquibase(prop.getChangeLogFile(), new ClassLoaderResourceAccessor(), database);

            database.setLiquibaseSchemaName(prop.getLiquibaseSchemaName());
            _liquibase.update();
            log.info("Migration is completed successfully");
        } catch (LiquibaseException e) {
            log.error("SQL Exception in migration " + e.getMessage());
        }
    }
}
