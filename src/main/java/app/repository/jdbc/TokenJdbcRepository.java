package app.repository.jdbc;

import app.entity.Token;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TokenJdbcRepository implements TokenRepository {

    private static final Logger log = LoggerFactory.getLogger(TokenJdbcRepository.class);
    private final Connection connection;

    public TokenJdbcRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Token> findById(Long id) {
        String sql = "SELECT * FROM business.tokens WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToken(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding token by id: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding token", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Token> findByUserId(Long userId) {
        String sql = "SELECT * FROM business.tokens WHERE user_id = ? AND expired = false LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToken(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding token by userId: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding token by userId", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Token> getTokenByUserEmail(String email) {
        String sql = """
        SELECT t.* FROM business.tokens t
        JOIN business.users u ON t.user_id = u.id
        WHERE u.email = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToken(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error getting token by user email: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error retrieving token by email", e);
        }

        return Optional.empty();
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM business.tokens WHERE user_id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, userId);
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorDeleteSqlException("Token not found, nothing deleted.");
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorDeleteSqlException("Error deleting token", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorDeleteSqlException("Transaction management error for delete", e);
        }
    }

    @Override
    public Token save(Token entity) {
        String sql = """
                INSERT INTO business.tokens (id, user_id, value, expired)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    user_id = EXCLUDED.user_id,
                    value = EXCLUDED.value,
                    expired = EXCLUDED.expired
                RETURNING id
                """;

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                if (entity.getId() == null || entity.getId() == 0) {
                    entity.setId(getNextTokenId());
                }

                ps.setLong(1, entity.getId());
                ps.setLong(2, entity.getUserId());
                ps.setString(3, entity.getValue());
                ps.setBoolean(4, entity.isExpired());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorInsertSqlException("Saving token failed, no rows affected.");
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
                throw new ErrorInsertSqlException("Error saving token", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorInsertSqlException("Transaction management error for save", e);
        }
    }

    @Override
    public void delete(Token entity) {
        String sql = "DELETE FROM business.tokens WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, entity.getId());
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorDeleteSqlException("Token not found, nothing deleted.");
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorDeleteSqlException("Error deleting token", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorDeleteSqlException("Transaction management error for delete", e);
        }
    }

    @Override
    public Collection<Token> getAll() {
        String sql = "SELECT * FROM business.tokens";
        List<Token> tokens = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(mapToken(rs));
                }
                connection.commit();
            } catch (SQLException e) {
                rollbackQuietly();
                throw new ErrorSelectSqlException("Error fetching all tokens", e);
            } finally {
                resetAutoCommit();
            }
        } catch (SQLException e) {
            throw new ErrorSelectSqlException("Transaction management error for getAll", e);
        }
        return tokens;
    }

    private Token mapToken(ResultSet rs) throws SQLException {
        return new Token(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("value"),
                rs.getBoolean("expired")
        );
    }

    private Long getNextTokenId() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NEXTVAL('transaction_id_seq')")) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Unable to get next value from sequence");
            }
        }
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
