package app.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.springdoc", "app.controller"})
@Import({org.springdoc.core.configuration.SpringDocConfiguration.class, org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
        org.springdoc.webmvc.ui.SwaggerConfig.class,
        org.springdoc.core.properties.SwaggerUiConfigProperties.class,
        org.springdoc.core.properties.SwaggerUiOAuthProperties.class,
        org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
})
@OpenAPIDefinition(
        info = @Info(
                title = "API для авторизации",
                version = "1.0",
                description = "API для регистрации, авторизации и выхода из системы",
                contact = @Contact(name = "Neket", email = "nekett@email.com", url = "https://neket.com"),
                license = @License(name = "Apache 2.0", url = "http://springdoc.org")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Локальный сервер")
        }
)

@OpenAPI31
public class OpenApiConfig implements WebMvcConfigurer {
    @Bean
    public GroupedOpenApi dafault() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("app.controller")
                .pathsToMatch("/")
                .build();
    }

}