package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertyConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertyConfig = new PropertySourcesPlaceholderConfigurer();
        propertyConfig.setLocation(new ClassPathResource("application.yml"));
        return propertyConfig;
    }

}
