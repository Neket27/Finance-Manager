package test.integration.db;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import test.integration.db.config.LiquibaseConfig;
import test.integration.db.config.PostgresContainer;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
public class TestDatabase {

    public final JdbcTemplate getJdbcTemplate;
    public final PostgresContainer container;
    public final LiquibaseConfig liquibaseConfig;

    public void stop() {
        container.stopContainer();
    }

    public void clear() {
        liquibaseConfig.resetAndMigrate();

    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return getJdbcTemplate;
    }

    @Bean
    public PostgresContainer postgresContainer() {
        return container;
    }

    @Bean
    LiquibaseConfig liquibaseConfig() {
        return liquibaseConfig;
    }

}

