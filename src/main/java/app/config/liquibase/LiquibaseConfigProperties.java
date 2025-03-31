package app.config.liquibase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "liquibase")
public class LiquibaseConfigProperties {

    private String changeLogFile;
    private String schemaName;

}
