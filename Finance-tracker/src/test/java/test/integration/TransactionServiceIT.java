package test.integration;

import app.App;
import app.dto.transaction.FilterTransactionDto;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.service.TransactionService;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.integration.db.config.LiquibaseConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
@Testcontainers
@Import({LiquibaseConfig.class})
class TransactionServiceIT {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LiquibaseConfig liquibaseConfig;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();

        // Настройки для datasource
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Настройки для liquibase
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "db/test-changelog/changelog-master.yml");
    }

    @BeforeEach
    void setup() {
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
    }

    @AfterEach
    void tearDown() {
        liquibaseConfig.dropSchemas();
        liquibaseConfig.initialize();
        UserContext.clear();
    }

    @Test
    void createTransaction_shouldAddTransactionSuccessfully() {
        // Act
        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Transaction transaction = transactionService.create(1L, createTransaction);

        // Assert
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals(BigDecimal.valueOf(150).stripTrailingZeros(), transaction.getAmount().stripTrailingZeros());
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

        Transaction t = transactionService.create(1L, createTransaction);

        // Act
        Transaction transaction = transactionService.getTransactionById(t.getId());

        // Assert
        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
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

        Transaction transaction = transactionService.create(1L, createTransaction);

        Transaction updateTransaction = Transaction.builder()
                .id(transaction.getId())
                .amount(BigDecimal.valueOf(200.00))
                .category("Bills")
                .description("Electricity bill")
                .date(Instant.now())
                .typeTransaction(TypeTransaction.EXPENSE)
                .financeId(1L)
                .build();


        // Act
        Transaction updatedTransaction = transactionService.edit(updateTransaction);

        // Assert
        assertNotNull(updatedTransaction);
        assertEquals(BigDecimal.valueOf(200).stripTrailingZeros(), updatedTransaction.getAmount().stripTrailingZeros());
        assertEquals("Bills", updatedTransaction.getCategory());
        assertEquals("Electricity bill", updatedTransaction.getDescription());
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
        transactionService.create(1L, createTransaction);

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
        transactionService.create(1L, createTransaction);

        // Act
        List<Transaction> transactions = transactionService.getFilteredTransactions(new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now(), "Groceries", "EXPENSE"));

        // Assert
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals("Groceries", transactions.get(0).getCategory());
    }

    @Test
    void filterTransactions_shouldReturnEmptyListWhenNoMatch() {
        // Act
        List<Transaction> transactions = transactionService.getFilteredTransactions(new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now(), "Entertainment", "EXPENSE"));

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void editTransaction_shouldFailIfTransactionNotFound() {
        // Act & Assert
        Transaction updateTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();
        assertThrows(RuntimeException.class, () -> transactionService.edit(updateTransaction));
    }

}
