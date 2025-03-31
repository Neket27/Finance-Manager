package app.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan("app")
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan
public class ApplicationConfig {

}
