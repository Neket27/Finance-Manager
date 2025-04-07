package app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "custom.liquibase")
@Getter
@Setter
public class LiquibaseConfigProperties {

    private List<String> createSchemaLocations;
}
