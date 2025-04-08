package app.repository.jdbc;

import app.entity.Token;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TokenJdbcRepository implements TokenRepository {

    private static final Logger log = LoggerFactory.getLogger(TokenJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public TokenJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Token> tokenRowMapper = (rs, rowNum) ->
            new Token(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("value"),
                    rs.getBoolean("expired")
            );

    @Override
    public Optional<Token> findById(Long id) {
        String sql = "SELECT * FROM business.tokens WHERE id = ?";
        try {
            return jdbcTemplate.query(sql, tokenRowMapper, id).stream().findFirst();
        } catch (Exception e) {
            log.error("Error finding token by id: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding token", e);
        }
    }

    @Override
    public Optional<Token> findByUserId(Long userId) {
        String sql = "SELECT * FROM business.tokens WHERE user_id = ? AND expired = false LIMIT 1";
        try {
            return jdbcTemplate.query(sql, tokenRowMapper, userId).stream().findFirst();
        } catch (Exception e) {
            log.error("Error finding token by userId: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding token by userId", e);
        }
    }

    @Override
    public Optional<Token> getTokenByUserEmail(String email) {
        String sql = """
                SELECT t.* FROM business.tokens t
                JOIN business.users u ON t.user_id = u.id
                WHERE u.email = ?
                """;
        try {
            return jdbcTemplate.query(sql, tokenRowMapper, email).stream().findFirst();
        } catch (Exception e) {
            log.error("Error getting token by user email: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error retrieving token by email", e);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM business.tokens WHERE user_id = ?";
        try {
            int affectedRows = jdbcTemplate.update(sql, userId);
            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("Token not found, nothing deleted.");
            }
        } catch (Exception e) {
            throw new ErrorDeleteSqlException("Error deleting token", e);
        }
    }

    @Override
    public Token save(Token entity) {
        String sql = """
                INSERT INTO business.tokens (user_id, value, expired)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int update = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entity.getUserId());
            ps.setString(2, entity.getValue());
            ps.setBoolean(3, entity.isExpired());
            return ps;
        }, keyHolder);

        if (update > 0) {
            List<Map<String, Object>> keys = keyHolder.getKeyList();
            if (!keys.isEmpty()) {
                Map<String, Object> generatedKey = keys.get(0);
                Number generatedId = (Number) generatedKey.get("id");
                if (generatedId != null) {
                    long id = generatedId.longValue();
                    entity.setId(id);
                    return entity;
                }
            }
        }

        return entity;
    }

@Override
public void delete(Token entity) {
    String sql = "DELETE FROM business.tokens WHERE id = ?";
    try {
        int affectedRows = jdbcTemplate.update(sql, entity.getId());
        if (affectedRows == 0) {
            throw new ErrorDeleteSqlException("Token not found, nothing deleted.");
        }
    } catch (Exception e) {
        throw new ErrorDeleteSqlException("Error deleting token", e);
    }
}

@Override
public Collection<Token> getAll() {
    String sql = "SELECT * FROM business.tokens";
    try {
        return jdbcTemplate.query(sql, tokenRowMapper);
    } catch (Exception e) {
        throw new ErrorSelectSqlException("Error fetching all tokens", e);
    }
}
}