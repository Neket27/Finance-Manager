package app.repository.jdbc;

import app.container.Component;
import app.entity.Finance;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.FinanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class FinanceJdbcRepository implements FinanceRepository {

    private static final Logger log = LoggerFactory.getLogger(FinanceJdbcRepository.class);
    private final Connection connection;

    public FinanceJdbcRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Finance> findById(Long id) {
        String sql = "SELECT * FROM business.finances WHERE id = ?";

        try {
            beginTransaction();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        commitTransaction();
                        return Optional.of(mapFinance(resultSet));
                    }
                }
                commitTransaction();
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding finance record by ID", e);
        } finally {
            resetAutoCommit();
        }
        return Optional.empty();
    }

    @Override
    public Finance save(Finance entity) {
        String financeSql = """
                    INSERT INTO business.finances (id, monthly_budget, savings_goal, current_savings, total_expenses)
                    VALUES (COALESCE(?, NEXTVAL('finance_id_seq')), ?, ?, ?, ?)
                    ON CONFLICT (id)
                    DO UPDATE SET
                        monthly_budget = EXCLUDED.monthly_budget,
                        savings_goal = EXCLUDED.savings_goal,
                        current_savings = EXCLUDED.current_savings,
                        total_expenses = EXCLUDED.total_expenses
                    RETURNING id
                """;

        try {
            beginTransaction();
            if (entity.getId() == null || entity.getId() == 0) {
                entity.setId(getNextFinanceId());
            }

            try (PreparedStatement financeStmt = connection.prepareStatement(financeSql, Statement.RETURN_GENERATED_KEYS)) {
                financeStmt.setLong(1, entity.getId());
                financeStmt.setBigDecimal(2, entity.getMonthlyBudget());
                financeStmt.setBigDecimal(3, entity.getSavingsGoal());
                financeStmt.setBigDecimal(4, entity.getCurrentSavings());
                financeStmt.setBigDecimal(5, entity.getTotalExpenses());

                int affectedRows = financeStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorInsertSqlException("Upserting finance record failed, no rows affected.");
                }

                try (ResultSet generatedKeys = financeStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }

                commitTransaction();
                return entity;
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error saving finance record: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error saving finance record into database", e);
        } finally {
            resetAutoCommit();
        }
    }

    @Override
    public void delete(Finance entity) {
        String sql = "DELETE FROM business.finances WHERE id = ?";

        try {
            beginTransaction();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, entity.getId());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorDeleteSqlException("Finance record not found, nothing deleted.");
                }

                commitTransaction();
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error deleting finance record: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting finance record from database", e);
        } finally {
            resetAutoCommit();
        }
    }

    @Override
    public Collection<Finance> getAll() {
        String sql = "SELECT * FROM business.finances";
        List<Finance> finances = new ArrayList<>();

        try {
            beginTransaction();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    finances.add(mapFinance(resultSet));
                }

                commitTransaction();
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error fetching all finance records: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all finance records from database", e);
        } finally {
            resetAutoCommit();
        }
        return finances;
    }

    private Finance mapFinance(ResultSet resultSet) throws SQLException {
        Finance finance = new Finance();
        finance.setId(resultSet.getLong("id"));
        finance.setMonthlyBudget(resultSet.getBigDecimal("monthly_budget"));
        finance.setSavingsGoal(resultSet.getBigDecimal("savings_goal"));
        finance.setCurrentSavings(resultSet.getBigDecimal("current_savings"));
        finance.setTotalExpenses(resultSet.getBigDecimal("total_expenses"));
        return finance;
    }


    private Long getNextFinanceId() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NEXTVAL('finance_id_seq')")) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Unable to get next value from sequence");
            }
        }
    }

    private void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    private void commitTransaction() throws SQLException {
        connection.commit();
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            log.error("Rollback failed: {}", ex.getMessage());
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
