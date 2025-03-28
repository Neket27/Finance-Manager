package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5434/db_finance_manager");
        dataSource.setUsername("db_finance_manager");
        dataSource.setPassword("db_finance_manager");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DriverManagerDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}