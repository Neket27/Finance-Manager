package test.integration;

import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.mapper.TransactionMapper;
import app.repository.jdbc.TransactionJdbcRepository;
import app.service.TransactionService;
import app.service.impl.TransactionServiceImpl;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import test.integration.db.TestDatabase;
import test.integration.db.TestDatabaseFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceIT {

    private TransactionService transactionService;
    private TestDatabase database;

    @BeforeEach
    void setup() {
        database = TestDatabaseFactory.create();

        User user = User.builder()
                .id(1L)
                .name("name")
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .financeId(1L)
                .role(Role.USER)
                .build();

        UserContext.setCurrentUser(user);
        transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(database.jdbcTemplate()), Mappers.getMapper(TransactionMapper.class));
    }

    @AfterEach
    void tearDown() {
        TestDatabaseFactory.reset();
        UserContext.clear();
    }

    @Test
    void createTransaction_shouldAddTransactionSuccessfully() {
        // Act
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        // Assert
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals(new BigDecimal("150.00"), transaction.getAmount());
        assertEquals("Groceries", transaction.getCategory());
        assertEquals("Supermarket purchase", transaction.getDescription());
        assertEquals(TypeTransaction.EXPENSE, transaction.getTypeTransaction());
    }

    @Test
    void getTransactionById_shouldReturnTransaction() {
        // Arrange
        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        transactionService.create(1L, createTransaction);

        // Act
        TransactionDto transaction = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(transaction);
        assertEquals(1L, transaction.id());
    }

    @Test
    void editTransaction_shouldUpdateTransactionDetails() {
        // Arrange
        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        transactionService.create(1L,createTransaction);


        // Act
        TransactionDto updatedTransaction = transactionService.edit(new UpdateTransactionDto(1L, new BigDecimal("200.00"), "Bills", Instant.now(), "Electricity bill", TypeTransaction.EXPENSE));

        // Assert
        assertNotNull(updatedTransaction);
        assertEquals(new BigDecimal("200.00"), updatedTransaction.amount());
        assertEquals("Bills", updatedTransaction.category());
        assertEquals("Electricity bill", updatedTransaction.description());
    }

    @Test
    void deleteTransaction_shouldRemoveTransaction() {
        // Arrange
        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();
        transactionService.create(1L,createTransaction);

        // Act
        assertDoesNotThrow(() -> transactionService.delete(1L));

        // Assert
        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(1L));
        assertTrue(exception.getMessage().contains("Transaction not found"));
    }

    @Test
    void filterTransactions_shouldReturnMatchingTransactions() {
        // Arrange
        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();
        transactionService.create(1L,createTransaction);

        // Act
        List<TransactionDto> transactions = transactionService.getFilteredTransactions(new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now(), "Groceries", "EXPENSE"));

        // Assert
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals("Groceries", transactions.get(0).category());
    }

    @Test
    void filterTransactions_shouldReturnEmptyListWhenNoMatch() {
        // Act
        List<TransactionDto> transactions = transactionService.getFilteredTransactions(new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now(), "Entertainment", "EXPENSE"));

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void editTransaction_shouldFailIfTransactionNotFound() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> transactionService.edit(new UpdateTransactionDto(999L, new BigDecimal("200.00"), "Bills", Instant.now(), "Electricity bill", TypeTransaction.EXPENSE)));
    }

}
