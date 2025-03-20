package app.repository.jdbc;

import app.entity.Role;
import app.entity.User;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);
    private final Connection connection;

    public UserJdbcRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM business.users WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM business.users WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing findByEmail: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User entity) {
        String sql = """
                INSERT INTO business.users (id, email, name, password, is_active, role, finance_id) 
                VALUES (?, ?, ?, ?, ?, ?, ?) 
                ON CONFLICT (id) DO UPDATE 
                SET email = EXCLUDED.email,
                    name = EXCLUDED.name,
                    password = EXCLUDED.password,
                    is_active = EXCLUDED.is_active,
                    role = EXCLUDED.role,
                    finance_id = EXCLUDED.finance_id
                RETURNING id
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Генерация ID, если он не установлен
            if (entity.getId() == null || entity.getId() == 0) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT NEXTVAL('transaction_id_seq')")) {
                    if (rs.next()) {
                        entity.setId(rs.getLong(1));
                    } else {
                        throw new SQLException("Unable to get next value from sequence");
                    }
                }
            }

            preparedStatement.setLong(1, entity.getId());
            preparedStatement.setString(2, entity.getEmail());
            preparedStatement.setString(3, entity.getName());
            preparedStatement.setString(4, entity.getPassword());
            preparedStatement.setBoolean(5, entity.isActive());
            preparedStatement.setString(6, entity.getRole().toString());
            preparedStatement.setObject(7, entity.getFinanceId());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new ErrorInsertSqlException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            log.error("Error inserting user: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error inserting user into database", e);
        }
    }

    @Override
    public void delete(User entity) {
        String sql = "DELETE FROM business.users WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, entity.getEmail());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("User not found, nothing deleted.");
            }
        } catch (SQLException e) {
            log.error("Error deleting user: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting user from database", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM business.users WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Error checking existsByEmail: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error checking if user exists", e);
        }
        return false;
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM business.users";
        List<User> users = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException e) {
            log.error("Error fetching all users: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all users from database", e);
        }
        return users;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setEmail(resultSet.getString("email"));
        user.setName(resultSet.getString("name"));
        user.setPassword(resultSet.getString("password"));
        user.setActive(resultSet.getBoolean("is_active"));

        String roleStr = resultSet.getString("role");

        user.setRole(Role.valueOf(roleStr.toUpperCase()));
        user.setFinanceId(resultSet.getObject("finance_id", Long.class));
        return user;
    }
}
