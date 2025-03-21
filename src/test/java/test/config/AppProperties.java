package test.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AppProperties {

    @JsonProperty("postgres-container")
    private PostgresContainerProperties postgresContainer;
    private LiquibaseProperties liquibase;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PostgresContainerProperties {

        @JsonProperty("db-name")
        private String dbName;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LiquibaseProperties {

        @JsonProperty("change-log-file")
        private String changeLogFile;

        @JsonProperty("schema-name")
        private String liquibaseSchemaName;
    }
}

