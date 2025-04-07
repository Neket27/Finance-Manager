package app.repository.jdbc;

import app.entity.Finance;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.FinanceRepository;
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
public class FinanceJdbcRepository implements FinanceRepository {

    private static final Logger log = LoggerFactory.getLogger(FinanceJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public FinanceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Finance> financeRowMapper = (rs, rowNum) -> {
        Finance finance = new Finance();
        finance.setId(rs.getLong("id"));
        finance.setMonthlyBudget(rs.getBigDecimal("monthly_budget"));
        finance.setSavingsGoal(rs.getBigDecimal("savings_goal"));
        finance.setCurrentSavings(rs.getBigDecimal("current_savings"));
        finance.setTotalExpenses(rs.getBigDecimal("total_expenses"));
        return finance;
    };

    @Override
    public Optional<Finance> findById(Long id) {
        try {
            String sql = "SELECT * FROM business.finances WHERE id = ?";
            return jdbcTemplate.query(sql, financeRowMapper, id)
                    .stream()
                    .findFirst();
        } catch (Exception e) {
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding finance record by ID", e);
        }
    }

    @Override
    public Finance save(Finance entity) {
        String sql = """
                    INSERT INTO business.finances (monthly_budget, savings_goal, current_savings, total_expenses)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (id)
                    DO UPDATE SET
                        monthly_budget = EXCLUDED.monthly_budget,
                        savings_goal = EXCLUDED.savings_goal,
                        current_savings = EXCLUDED.current_savings,
                        total_expenses = EXCLUDED.total_expenses
                    RETURNING id
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int update = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, entity.getMonthlyBudget());
            ps.setBigDecimal(2, entity.getSavingsGoal());
            ps.setBigDecimal(3, entity.getCurrentSavings());
            ps.setBigDecimal(4, entity.getTotalExpenses());
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
    public void delete(Finance entity) {
        try {
            String sql = "DELETE FROM business.finances WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, entity.getId());

            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("Finance record not found, nothing deleted.");
            }
        } catch (Exception e) {
            log.error("Error deleting finance record: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting finance record from database", e);
        }
    }

    @Override
    public Collection<Finance> getAll() {
        try {
            String sql = "SELECT * FROM business.finances";
            return jdbcTemplate.query(sql, financeRowMapper);
        } catch (Exception e) {
            log.error("Error fetching all finance records: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all finance records from database", e);
        }
    }
}
