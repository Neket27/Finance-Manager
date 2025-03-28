package app.repository.jdbc;

import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
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
    @Transactional
    public Transaction save(Transaction entity) {
        try {
            String sql = """
                INSERT INTO business.transactions (id, amount, category, date, description, type_transaction, finance_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE 
                SET amount = EXCLUDED.amount, 
                    category = EXCLUDED.category, 
                    date = EXCLUDED.date, 
                    description = EXCLUDED.description, 
                    type_transaction = EXCLUDED.type_transaction, 
                    finance_id = EXCLUDED.finance_id
                RETURNING id
            """;

            if (entity.getId() == null || entity.getId() == 0) {
                entity.setId(jdbcTemplate.queryForObject("SELECT NEXTVAL('transaction_id_seq')", Long.class));
            }

            jdbcTemplate.update(sql, entity.getId(), entity.getAmount(), entity.getCategory(),
                    Timestamp.from(entity.getDate()), entity.getDescription(),
                    entity.getTypeTransaction().toString(), entity.getFinanceId());

            return entity;
        } catch (Exception e) {
            log.error("Error inserting or updating transaction: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error inserting or updating transaction into database", e);
        }
    }

    @Override
    @Transactional
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