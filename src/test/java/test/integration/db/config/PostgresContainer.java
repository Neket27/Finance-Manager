package test.integration.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresContainer {

    private final Logger log = LoggerFactory.getLogger(PostgresContainer.class);
    private static PostgreSQLContainer<?> postgres;
    private final AppProperties.PostgresContainerProperties prop;

    public PostgresContainer(AppProperties.PostgresContainerProperties prop) {
        this.prop = prop;
    }

    public void startContainer() {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName(prop.getDbName())
                .withUsername(prop.getUsername())
                .withPassword(prop.getPassword());
        postgres.start();
    }

    public void stopContainer() {
        if (postgres != null)
            postgres.stop();
        postgres = null;
    }

//    public Connection getConnection() {
//        try {
//            return DriverManager.getConnection(
//                    postgres.getJdbcUrl(),
//                    postgres.getUsername(),
//                    postgres.getPassword()
//            );
//        } catch (SQLException e) {
//            throw new RuntimeException("Could not connect to PostgreSQL", e);
//        }
//    }

    public DriverManagerDataSource driverManagerDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }


//    public void clear() {
//        try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
//
//            stmt.execute("SET lock_timeout TO '5s';");
//            stmt.execute("SET search_path TO business, metadata, public;");
//
//            List<String> tables = new ArrayList<>();
//            try (ResultSet rs = stmt.executeQuery(
//                    "SELECT tablename FROM pg_tables WHERE schemaname = 'business'")) {
//                while (rs.next()) {
//                    tables.add(rs.getString("tablename"));
//                }
//            }
//
//            if (!tables.isEmpty()) {
//                String truncateAll = tables.stream()
//                        .map(table -> "business." + table)
//                        .collect(Collectors.joining(", "));
//                stmt.execute("TRUNCATE TABLE " + truncateAll + " CASCADE;");
//            }
//
//            stmt.execute("TRUNCATE TABLE metadata.databasechangelog CASCADE;");
//            stmt.execute("TRUNCATE TABLE metadata.databasechangeloglock CASCADE;");
//
//            log.debug("Database cleared (business + metadata)");
//
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to clear database", e);
//        }
//    }

}
