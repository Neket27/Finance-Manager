package app.repository.jdbc;

import app.entity.Finance;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.FinanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
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
    @Transactional
    public Finance save(Finance entity) {
        String sql = """
                    INSERT INTO business.finances (id, monthly_budget, savings_goal, current_savings, total_expenses)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (id)
                    DO UPDATE SET
                        monthly_budget = EXCLUDED.monthly_budget,
                        savings_goal = EXCLUDED.savings_goal,
                        current_savings = EXCLUDED.current_savings,
                        total_expenses = EXCLUDED.total_expenses
                    RETURNING id
                """;

        try {
            Long id = entity.getId();
            if (id == null || id == 0) {
                id = jdbcTemplate.queryForObject("SELECT NEXTVAL('finance_id_seq')", Long.class);
                entity.setId(id);
            }

            Long returnedId = jdbcTemplate.queryForObject(sql, Long.class, id, entity.getMonthlyBudget(),
                    entity.getSavingsGoal(), entity.getCurrentSavings(), entity.getTotalExpenses());

            entity.setId(returnedId);
            return entity;

        } catch (Exception e) {
            log.error("Error saving finance record: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error saving finance record into database", e);
        }
    }


    @Override
    @Transactional
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
