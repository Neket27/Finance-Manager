package app.repository.jdbc;

import app.entity.Role;
import app.entity.User;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserJdbcRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            Long id = entity.getId();
            if (id == null || id == 0) {
                id = jdbcTemplate.queryForObject("SELECT NEXTVAL('user_id_seq')", Long.class);
                entity.setId(id);
            }

            Long returnedId = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    entity.getId(),
                    entity.getEmail(),
                    entity.getName(),
                    entity.getPassword(),
                    entity.isActive(),
                    entity.getRole().name(),
                    entity.getFinanceId()
            );
            entity.setId(returnedId);
            return entity;
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error saving user", e);
        }
    }

    @Override
    public void delete(User entity) {
        String sql = "DELETE FROM business.users WHERE email = ?";
        try {
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
                Role.valueOf(rs.getString("role").toUpperCase()),
                rs.getLong("finance_id"),
                rs.getBoolean("is_active")
        );
    }
}
