package app.repository.jdbc;

import app.container.Component;
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

@Component
public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);
    private final Connection connection;

    public UserJdbcRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findUserBy("id = ?", id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findUserBy("email = ?", email);
    }

    private Optional<User> findUserBy(String condition, Object param) {
        String sql = "SELECT * FROM business.users WHERE " + condition;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing findUserBy [{}]: {}", condition, e.getMessage());
            throw new ErrorSelectSqlException("Error finding user", e);
        }
        return Optional.empty();
    }

    @Override
    public User save(User entity) {
        String sql = """
                INSERT INTO business.users (id, email, name, password, is_active, role, finance_id) 
                VALUES (?, ?, ?, ?, ?, ?, ?) 
                ON CONFLICT (id) DO UPDATE SET 
                    email = EXCLUDED.email,
                    name = EXCLUDED.name,
                    password = EXCLUDED.password,
                    is_active = EXCLUDED.is_active,
                    role = EXCLUDED.role,
                    finance_id = EXCLUDED.finance_id
                RETURNING id
                """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                if (entity.getId() == null || entity.getId() == 0) {
                    entity.setId(getNextUserId());
                }

                ps.setLong(1, entity.getId());
                ps.setString(2, entity.getEmail());
                ps.setString(3, entity.getName());
                ps.setString(4, entity.getPassword());
                ps.setBoolean(5, entity.isActive());
                ps.setString(6, entity.getRole().name());
                ps.setObject(7, entity.getFinanceId());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorInsertSqlException("Saving user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }

                connection.commit();
                return entity;
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorInsertSqlException("Error saving user into database", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorInsertSqlException("Transaction management error for save", e);
        }
    }

    @Override
    public void delete(User entity) {
        String sql = "DELETE FROM business.users WHERE email = ?";
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, entity.getEmail());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorDeleteSqlException("User not found, nothing deleted.");
                }

                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorDeleteSqlException("Error deleting user from database", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorDeleteSqlException("Transaction management error for delete", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM business.users WHERE email = ? LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error checking existence by email: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error checking if user exists", e);
        }
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM business.users";
        List<User> users = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    users.add(mapUser(rs));
                }

                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorSelectSqlException("Error fetching all users from database", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorSelectSqlException("Transaction management error for getAll", e);
        }
        return users;
    }

    private Long getNextUserId() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NEXTVAL('transaction_id_seq')")) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Unable to get next value from sequence");
            }
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role").toUpperCase()),
                rs.getLong("finance_id"),
                rs.getBoolean("is_active")
        );
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
            log.warn("Transaction rolled back");
        } catch (SQLException rollbackEx) {
            log.error("Rollback failed: {}", rollbackEx.getMessage());
        }
    }

    private void resetAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.error("Failed to reset auto-commit: {}", e.getMessage());
        }
    }
}
