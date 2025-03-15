package app.repository.bd;

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

import static app.config.DbConfig.*;

public class TransactionJdbcRepository implements TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionJdbcRepository.class);

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM business.transactions WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Error executing findById: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error finding transaction by ID", e);
        }
        return Optional.empty();
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

        String sqlFinanceTransaction = """
        INSERT INTO business.finance_transactions (finance_id, transaction_id) 
        VALUES (?, ?) 
        """;

        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatementTransaction = connection.prepareStatement(sqlTransaction, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement preparedStatementFinanceTransaction = connection.prepareStatement(sqlFinanceTransaction)) {

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

            preparedStatementTransaction.setLong(1, entity.getId());
            preparedStatementTransaction.setDouble(2, entity.getAmount());
            preparedStatementTransaction.setString(3, entity.getCategory());
            preparedStatementTransaction.setTimestamp(4, Timestamp.from(entity.getDate()));
            preparedStatementTransaction.setString(5, entity.getDescription());
            preparedStatementTransaction.setString(6, entity.getTypeTransaction().toString());
            preparedStatementTransaction.setLong(7, entity.getFinanceId());

            int affectedRows = preparedStatementTransaction.executeUpdate();
            if (affectedRows == 0) {
                throw new ErrorInsertSqlException("Creating or updating transaction failed, no rows affected.");
            }

            preparedStatementFinanceTransaction.setLong(1, entity.getFinanceId());
            preparedStatementFinanceTransaction.setLong(2, entity.getId());
            preparedStatementFinanceTransaction.executeUpdate();

            connection.commit();
            return entity;
        } catch (SQLException e) {
            log.error("Error inserting or updating transaction: {}", e.getMessage());
            throw new ErrorInsertSqlException("Error inserting or updating transaction into database", e);
        }
    }



    @Override
    public void delete(Transaction entity) {
        deleteById(entity.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM business.transactions WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new ErrorDeleteSqlException("Transaction not found, nothing deleted.");
            }
        } catch (SQLException e) {
            log.error("Error deleting transaction: {}", e.getMessage());
            throw new ErrorDeleteSqlException("Error deleting transaction from database", e);
        }
    }

    @Override
    public List<Transaction> findByFinanceId(Long id) {
        String sql = """
                    SELECT t.* FROM business.transactions t
                    JOIN business.finance_transactions ft ON t.id = ft.transaction_id
                    WHERE ft.finance_id = ?
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet)); // Теперь получаем сами транзакции
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching transactions for finance ID {}: {}", id, e.getMessage());
            throw new ErrorSelectSqlException("Error fetching transactions from database", e);
        }
        return transactions;
    }

    @Override
    public List<Transaction> getFilteredTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction) {
        StringBuilder sqlBuilder = new StringBuilder("""
                        SELECT t.id, t.amount, t.category, t.date, t.description, t.type_transaction
                        FROM business.transactions t
                        JOIN business.finance_transactions ft ON t.id = ft.transaction_id
                        WHERE ft.finance_id = ?
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(financeId);

        boolean hasFilters = false;

        if (startDate != null) {
            sqlBuilder.append(" AND t.date >= ?");
            parameters.add(Timestamp.from(startDate));
            hasFilters = true;
        }

        if (endDate != null) {
            sqlBuilder.append(" AND t.date <= ?");
            parameters.add(Timestamp.from(endDate));
            hasFilters = true;
        }

        if (!category.isEmpty()) {
            sqlBuilder.append(" AND t.category = ?");
            parameters.add(category);
            hasFilters = true;
        }

        if (typeTransaction != null) {
            sqlBuilder.append(" AND t.type_transaction = ?");
            parameters.add(typeTransaction.toString());
            hasFilters = true;
        }

        if (!hasFilters) {
            sqlBuilder = new StringBuilder("""
                            SELECT t.id, t.amount, t.category, t.date, t.description, t.type_transaction
                            FROM business.transactions t
                            JOIN business.finance_transactions ft ON t.id = ft.transaction_id
                            WHERE ft.finance_id = ?
                    """);
            parameters.clear();
            parameters.add(financeId);
        }

        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (resultSet.next())
                    transactions.add(mapTransaction(resultSet));

                return transactions;
            }
        } catch (SQLException e) {
            log.error("Error fetching filtered transactions: {}", e.getMessage());
            throw new RuntimeException("Error fetching transactions from database", e);
        }
    }


    @Override
    public Collection<Transaction> getAll() {
        String sql = "SELECT * FROM business.transactions";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                transactions.add(mapTransaction(resultSet));
            }
        } catch (SQLException e) {
            log.error("Error fetching all transactions: {}", e.getMessage());
            throw new ErrorSelectSqlException("Error fetching all transactions from database", e);
        }
        return transactions;
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getLong("id"));
        transaction.setAmount(resultSet.getDouble("amount"));
        transaction.setCategory(resultSet.getString("category"));
        transaction.setDate(resultSet.getTimestamp("date").toInstant());
        transaction.setDescription(resultSet.getString("description"));
        transaction.setTypeTransaction(TypeTransaction.valueOf(resultSet.getString("type_transaction")));
        transaction.setFinanceId(resultSet.getLong("finance_id"));
        return transaction;
    }
}
