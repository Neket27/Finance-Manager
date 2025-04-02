package app.repository.jdbc;

import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TransactionJdbcRepository implements TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionJdbcRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public TransactionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setCategory(rs.getString("category"));
        transaction.setDate(rs.getTimestamp("date").toInstant());
        transaction.setDescription(rs.getString("description"));
        transaction.setTypeTransaction(TypeTransaction.valueOf(rs.getString("type_transaction").toUpperCase()));
        transaction.setFinanceId(rs.getLong("finance_id"));
        return transaction;
    };

    @Override
    public Optional<Transaction> findById(Long id) {
        try {
            String sql = "SELECT * FROM business.transactions WHERE id = ?";
            return jdbcTemplate.query(sql, transactionRowMapper, id).stream().findFirst();
        } catch (Exception e) {
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding transaction by ID", e);
        }
    }

    @Override
    public List<Transaction> findByFinanceId(Long id) {
        try {
            String sql = "SELECT * FROM business.transactions WHERE finance_id = ?";
            return jdbcTemplate.query(sql, transactionRowMapper, id);
        } catch (Exception e) {
            log.error("Error fetching transactions for finance ID {}: {}", id, e.getMessage());
            throw new ErrorSelectSqlException("Error fetching transactions from database", e);
        }
    }

    @Override
    public List<Transaction> getFilteredTransactions(Long financeId, Instant startDate, Instant endDate, String category, String typeTransaction) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM business.transactions WHERE finance_id = ?");

        if (startDate != null) sqlBuilder.append(" AND date >= ?");
        if (endDate != null) sqlBuilder.append(" AND date <= ?");
        if (!category.isEmpty()) sqlBuilder.append(" AND category = ?");
        if (!typeTransaction.isEmpty()) sqlBuilder.append(" AND type_transaction = ?");

        return jdbcTemplate.query(sqlBuilder.toString(), transactionRowMapper, financeId, Timestamp.from(startDate), Timestamp.from(endDate), category, typeTransaction);
    }

    @Override
    public Collection<Transaction> getAll() {
        try {
            String sql = "SELECT * FROM business.transactions";
            return jdbcTemplate.query(sql, transactionRowMapper);
        } catch (Exception e) {
            log.error("Error fetching all transactions: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all transactions from database", e);
        }
    }

    @Override
    public Transaction save(Transaction entity) {
        String sql = """
                    INSERT INTO business.transactions (amount, category, date, description, type_transaction, finance_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int update = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, entity.getAmount());
            ps.setString(2, entity.getCategory());
            ps.setTimestamp(3, Timestamp.from(entity.getDate()));
            ps.setString(4, entity.getDescription());
            ps.setString(5, entity.getTypeTransaction().toString());
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
    public void deleteById(Long id) {
        try {
            String sql = "DELETE FROM business.transactions WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, id);
            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("Transaction not found, nothing deleted.");
            }
        } catch (Exception e) {
            log.error("Error deleting transaction: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting transaction from database", e);
        }
    }

    @Override
    public void delete(Transaction entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAllByFinanceId(Long financeId) {
        try {
            String sql = "DELETE FROM business.transactions WHERE finance_id = ?";
            jdbcTemplate.update(sql, financeId);
        } catch (Exception e) {
            log.error("Error deleting transactions for financeId {}: {}", financeId, e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting transactions from database", e);
        }
    }
}