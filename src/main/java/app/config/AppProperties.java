package app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AppProperties {

    private DbProperties db;
    private LiquibaseProperties liquibase;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DbProperties {
        private String url;
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

