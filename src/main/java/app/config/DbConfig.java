package app.config;

import app.exception.db.ErrorConnectionDb;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
@Setter
@AllArgsConstructor
public class DbConfig {

    private AppProperties.DbProperties prop;

    public Connection getConnection() {
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
