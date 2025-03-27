package app.config;

import app.container.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

@Configuration
public class AppProperties {

    private DbProperties db;
    private LiquibaseProperties liquibase;

    public DbProperties getDb() {
        return db;
    }

    public void setDb(DbProperties db) {
        this.db = db;
    }

    public LiquibaseProperties getLiquibase() {
        return liquibase;
    }

    public void setLiquibase(LiquibaseProperties liquibase) {
        this.liquibase = liquibase;
    }


    @Configuration
    public static class DbProperties {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    @Configuration
    public static class LiquibaseProperties {
        @JsonProperty("change-log-file")
        private String changeLogFile;

        @JsonProperty("schema-name")
        private String liquibaseSchemaName;

        public String getChangeLogFile() {
            return changeLogFile;
        }

        public void setChangeLogFile(String changeLogFile) {
            this.changeLogFile = changeLogFile;
        }

        public String getLiquibaseSchemaName() {
            return liquibaseSchemaName;
        }

        public void setLiquibaseSchemaName(String liquibaseSchemaName) {
            this.liquibaseSchemaName = liquibaseSchemaName;
        }
    }
}

