package app.repository.jdbc;

import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.db.ErrorDeleteSqlException;
import app.exception.db.ErrorInsertSqlException;
import app.exception.db.ErrorSelectSqlException;
import app.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TransactionJdbcRepository implements TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionJdbcRepository.class);
    private final Connection connection;

    public TransactionJdbcRepository(Connection connectionProvider) {
        this.connection = connectionProvider;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM business.transactions WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    if (resultSet.next()) {
                        return Optional.of(mapTransaction(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding transaction by ID", e);
        } finally {
            resetAutoCommit();
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByFinanceId(Long id) {
        String sql = """
                SELECT t.* FROM business.transactions t
                JOIN business.finances f ON t.finance_id = f.id
                WHERE t.finance_id = ?
                """;

        List<Transaction> transactions = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        transactions.add(mapTransaction(resultSet));
                    }
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error fetching transactions for finance ID {}: {}", id, e.getMessage());
            throw new ErrorSelectSqlException("Error fetching transactions from database", e);
        } finally {
            resetAutoCommit();
        }
        return transactions;
    }

    @Override
    public List<Transaction> getFilteredTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT t.id, t.amount, t.category, t.date, t.description, t.type_transaction, t.finance_id
                FROM business.transactions t
                JOIN business.finances f ON f.id = t.finance_id
                WHERE t.finance_id = ?
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(financeId);

        if (startDate != null) {
            sqlBuilder.append(" AND t.date >= ?");
            parameters.add(Timestamp.from(startDate));
        }

        if (endDate != null) {
            sqlBuilder.append(" AND t.date <= ?");
            parameters.add(Timestamp.from(endDate));
        }

        if (category != null && !category.isEmpty()) {
            sqlBuilder.append(" AND t.category = ?");
            parameters.add(category);
        }

        if (typeTransaction != null) {
            sqlBuilder.append(" AND t.type_transaction = ?");
            parameters.add(typeTransaction.toString());
        }

        List<Transaction> transactions = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        transactions.add(mapTransaction(resultSet));
                    }
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error fetching filtered transactions: {}", e.getMessage());
            throw new RuntimeException("Error fetching transactions from database", e);
        } finally {
            resetAutoCommit();
        }
        return transactions;
    }

    private boolean hasTransactions(Long financeId) {
        String sql = "SELECT COUNT(*) FROM business.transactions WHERE finance_id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, financeId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    connection.commit();
                    if (resultSet.next()) {
                        return resultSet.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error checking transactions for financeId {}: {}", financeId, e.getMessage());
            throw new RuntimeException("Error checking transactions from database", e);
        } finally {
            resetAutoCommit();
        }
        return false;
    }

    @Override
    public Collection<Transaction> getAll() {
        String sql = "SELECT * FROM business.transactions";
        List<Transaction> transactions = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
                connection.commit();
            }
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error fetching all transactions: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all transactions from database", e);
        } finally {
            resetAutoCommit();
        }
        return transactions;
    }

    @Override
    public Transaction save(Transaction entity) {
        String sqlTransaction = """
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
        try {
            connection.setAutoCommit(false);

            if (entity.getId() == null || entity.getId() == 0) {
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT NEXTVAL('transaction_id_seq')")) {
                    if (rs.next()) {
                        entity.setId(rs.getLong(1));
                    } else {
                        throw new SQLException("Unable to get next value from sequence");
                    }
                }
            }

            try (PreparedStatement preparedStatementTransaction = connection.prepareStatement(sqlTransaction, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatementTransaction.setLong(1, entity.getId());
                preparedStatementTransaction.setBigDecimal(2, entity.getAmount());
                preparedStatementTransaction.setString(3, entity.getCategory());
                preparedStatementTransaction.setTimestamp(4, Timestamp.from(entity.getDate()));
                preparedStatementTransaction.setString(5, entity.getDescription());
                preparedStatementTransaction.setString(6, entity.getTypeTransaction().toString());
                preparedStatementTransaction.setLong(7, entity.getFinanceId());

                int affectedRows = preparedStatementTransaction.executeUpdate();
                if (affectedRows == 0) {
                    throw new ErrorInsertSqlException("Creating or updating transaction failed, no rows affected.");
                }
            }

            connection.commit();
            return entity;
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error inserting or updating transaction: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error inserting or updating transaction into database", e);
        } finally {
            resetAutoCommit();
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM business.transactions WHERE id = ?";
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new ErrorDeleteSqlException("Transaction not found, nothing deleted.");
                }
            }

            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error deleting transaction: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting transaction from database", e);
        } finally {
            resetAutoCommit();
        }
    }

    @Override
    public void delete(Transaction entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAllByFinanceId(Long financeId) {
        if (financeId == null) {
            throw new IllegalArgumentException("financeId cannot be null");
        }

        if (!hasTransactions(financeId)) {
            log.warn("No transactions found for financeId: {}", financeId);
            return;
        }

        String sql = "DELETE FROM business.transactions WHERE finance_id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, financeId);
                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows == 0) {
                    log.warn("No transactions deleted for financeId: {}", financeId);
                    throw new ErrorDeleteSqlException("Transaction not found, nothing deleted.");
                }

                log.info("Deleted {} transactions for financeId: {}", affectedRows, financeId);
            }

            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            log.error("Error deleting transactions for financeId {}: {}", financeId, e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting transaction from database", e);
        } finally {
            resetAutoCommit();
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

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getLong("id"));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setCategory(resultSet.getString("category"));
        transaction.setDate(resultSet.getTimestamp("date").toInstant());
        transaction.setDescription(resultSet.getString("description"));

        String transactionTypeStr = resultSet.getString("type_transaction");

        transaction.setTypeTransaction(TypeTransaction.valueOf(transactionTypeStr.toUpperCase()));
        transaction.setFinanceId(resultSet.getLong("finance_id"));
        return transaction;
    }
}
