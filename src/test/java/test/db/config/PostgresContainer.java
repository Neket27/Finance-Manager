package test.db.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Testcontainers
public class PostgresContainer {

    private final Logger log = LoggerFactory.getLogger(PostgresContainer.class);
    private static PostgreSQLContainer<?> postgres;
    private static final String DB_NAME = "testdb";
    private static final String USER_NAME = "test";
    private static final String PASSWORD = "test";

    public void startContainer() {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName(DB_NAME)
                .withUsername(USER_NAME)
                .withPassword(PASSWORD);
        postgres.start();
    }

    public void stopContainer() {
        if (postgres != null)
            postgres.stop();
        postgres=null;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to PostgreSQL", e);
        }
    }


    public void clear() {
        try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {

            stmt.execute("SET lock_timeout TO '5s';");
            stmt.execute("SET search_path TO business, metadata, public;");

            List<String> tables = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT tablename FROM pg_tables WHERE schemaname = 'business'")) {
                while (rs.next()) {
                    tables.add(rs.getString("tablename"));
                }
            }

            if (!tables.isEmpty()) {
                String truncateAll = tables.stream()
                        .map(table -> "business." + table)
                        .collect(Collectors.joining(", "));
                stmt.execute("TRUNCATE TABLE " + truncateAll + " CASCADE;");
            }

            stmt.execute("TRUNCATE TABLE metadata.databasechangelog CASCADE;");
            stmt.execute("TRUNCATE TABLE metadata.databasechangeloglock CASCADE;");

            log.debug("Database cleared (business + metadata)");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }

}
