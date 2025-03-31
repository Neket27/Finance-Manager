package app.config.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "db")
public class DbConfigProperties {

    private String url;
    private String username;
    private String password;

}
