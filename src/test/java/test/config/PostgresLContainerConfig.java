package test.config;

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
public class PostgresLContainerConfig {

    private AppProperties.PostgresContainerProperties prop;

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(prop.getDbName(), prop.getUsername(), prop.getPassword());
        } catch (SQLException e) {
            throw new ErrorConnectionDb(e.getMessage());
        }
    }
}
