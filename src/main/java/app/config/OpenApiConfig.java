package app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = {"org.springdoc"})
@Import({SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class,
        JacksonAutoConfiguration.class})

@OpenAPI31
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact();
        contact.setName("Neket");
        contact.setEmail("nikitaivanovitc@gmail.com");

        License mitLicense = new License().name("Apache 2.0")
                .url("https://github.com/Neket27");

        Info info = new Info()
                .title("Финансовый сервис API")
                .version("1.0")
                .contact(contact)
                .description("Документация по взаимодействию с api сервиса")
                .termsOfService("http://localhost:8080")
                .license(mitLicense);

        return new OpenAPI().info(info);
    }
}




