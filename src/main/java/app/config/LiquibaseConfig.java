package app.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiquibaseConfig {

    private final String changeLogFile = "db/changelog/changelog-master.yml";
    private final String liquibaseSchemaName = "metadata";
    private final DbConfig dbConfig;

    public LiquibaseConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void connect() {
        try {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dbConfig.getConnection()));

            Liquibase liquibase =
                    new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);

            database.setLiquibaseSchemaName(liquibaseSchemaName);
            liquibase.update();
            System.out.println("Migration is completed successfully");
        } catch (LiquibaseException e) {
            System.out.println("SQL Exception in migration " + e.getMessage());
        }
    }
}