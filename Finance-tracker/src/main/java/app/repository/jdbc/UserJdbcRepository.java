package app.repository.jdbc;

import app.entity.Role;
import app.entity.User;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
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
public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean tableIsEmpty() {
        String sql = "SELECT 1 FROM business.users LIMIT 1";
        try {
            jdbcTemplate.queryForObject(sql, Integer.class);
            return false;
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
    }


    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM business.users WHERE id = ?";
        return queryForOptional(sql, id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM business.users WHERE email = ?";
        return queryForOptional(sql, email);
    }

    private Optional<User> queryForOptional(String sql, Object param) {
        try {
            List<User> users = jdbcTemplate.query(sql, userRowMapper(), param);
            return users.stream().findFirst();
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding user", e);
        }
    }

    @Override
    public User save(User entity) {
        String sql = """
                INSERT INTO business.users (name, email, password, is_active, role, finance_id)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (email) DO UPDATE SET
                    name = EXCLUDED.name,
                    password = EXCLUDED.password,
                    is_active = EXCLUDED.is_active,
                    role = EXCLUDED.role,
                    finance_id = EXCLUDED.finance_id
                RETURNING id
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int update = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPassword());
            ps.setBoolean(4, entity.getIsActive());
            ps.setString(5, entity.getRole().toString());
            ps.setLong(6, entity.getFinanceId());
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
    public void delete(User entity) {
        String sql = "DELETE FROM business.users WHERE email = ?";
        try {
            Collection<User> all = getAll();
            log.info(all.toString());
            int affectedRows = jdbcTemplate.update(sql, entity.getEmail());
            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("User not found, nothing deleted.");
            }
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting user", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT EXISTS (SELECT 1 FROM business.users WHERE email = ? LIMIT 1)";
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
        } catch (Exception e) {
            log.error("Error checking user existence: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error checking if user exists", e);
        }
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM business.users";
        try {
            return jdbcTemplate.query(sql, userRowMapper());
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all users", e);
        }
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getBoolean("is_active"),
                Role.valueOf(rs.getString("role").toUpperCase()),
                rs.getLong("finance_id")
        );
    }
}
