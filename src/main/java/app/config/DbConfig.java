package app.config;

import app.exception.db.ErrorConnectionDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    private String URL = "jdbc:postgresql://localhost:5434/db_finance_manager";
    private String USER_NAME = "db_finance_manager";
    private String PASSWORD = "db_finance_manager";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new ErrorConnectionDb(e.getMessage());
        }
    }
}

