package app.config;

import app.container.Bean;
import app.container.Configuration;
import app.exception.db.ErrorConnectionDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DbConfig {

    private AppProperties.DbProperties prop;

    public DbConfig(AppProperties.DbProperties prop) {
        this.prop = prop;
    }

    public AppProperties.DbProperties getProp() {
        return prop;
    }

    public void setProp(AppProperties.DbProperties prop) {
        this.prop = prop;
    }

    @Bean
    public Connection connection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(prop.getUrl(), prop.getUsername(), prop.getPassword());
        } catch (SQLException e) {
            throw new ErrorConnectionDb(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
