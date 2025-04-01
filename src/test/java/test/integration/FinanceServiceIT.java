package test.integration;

import app.context.UserContext;
import app.dto.finance.CreateFinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.TypeTransaction;
import app.exception.common.CreateException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.impl.FinanceServiceImpl;
import app.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import test.integration.db.TestDatabase;
import test.integration.db.TestDatabaseFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceIT {

    private FinanceService financeService;
    private TransactionService transactionService;
    private TestDatabase database;

    @BeforeEach
    void setup() {
        database = TestDatabaseFactory.create();

        UserDto userDto = new UserDto.Builder()
                .id(1L)
                .name("name")
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .finance(1L)
                .role(Role.USER)
                .build();

        UserContext.setCurrentUser(userDto);
        transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(database.jdbcTemplate()), Mappers.getMapper(TransactionMapper.class));
        financeService = new FinanceServiceImpl(new FinanceJdbcRepository(database.jdbcTemplate()), transactionService, Mappers.getMapper(FinanceMapper.class));
    }


    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
    }

    @Test
    void createTransaction_shouldAddTransactionSuccessfully() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of(1L)));
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                BigDecimal.valueOf(1000),
                "Food",
                "Groceries shopping",
                TypeTransaction.EXPENSE
        );

        // Act
        TransactionDto transaction = financeService.createTransaction(financeId, transactionDto);

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(1000), transaction.amount());
        assertEquals("Food", transaction.category());
        assertEquals("Groceries shopping", transaction.description());
    }

    @Test
    void createTransaction_shouldThrowExceptionWhenInsufficientFunds() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(500), BigDecimal.ZERO, List.of(1L)));
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                BigDecimal.valueOf(2000),
                "Rent",
                "Monthly rent payment",
                TypeTransaction.EXPENSE
        );

        // Act & Assert
        assertThrows(CreateException.class, () -> financeService.createTransaction(financeId, transactionDto));
    }

    @Test
    void getExpensesByCategory_shouldReturnCorrectExpenses() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of(1L)));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(1000), "Food", "Lunch", TypeTransaction.EXPENSE));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(500), "Entertainment", "Movie", TypeTransaction.EXPENSE));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(200), "Food", "Snacks", TypeTransaction.EXPENSE));

        // Act
        Map<String, BigDecimal> expenses = financeService.getExpensesByCategory(financeId);

        // Assert
        assertEquals(BigDecimal.valueOf(1200).stripTrailingZeros(), expenses.get("Food").stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(500).stripTrailingZeros(), expenses.get("Entertainment").stripTrailingZeros());
    }

    @Test
    void filterTransactions_shouldReturnCorrectTransactions() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of(1L)));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(1000), "Food", "Lunch", TypeTransaction.EXPENSE));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(500), "Entertainment", "Movie", TypeTransaction.EXPENSE));
        FilterTransactionDto filterDto = new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now().plusSeconds(86400), "Food", "EXPENSE");

        // Act
        List<TransactionDto> transactions = financeService.filterTransactions(financeId, filterDto);

        // Assert
        assertEquals(1, transactions.size());
        assertEquals("Food", transactions.get(0).category());
    }

    @Test
    void createTransaction_shouldReturnCorrectTransactionType() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), BigDecimal.valueOf(1000), BigDecimal.ZERO, List.of(1L)));
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                BigDecimal.valueOf(500),
                "Salary",
                "Monthly salary",
                TypeTransaction.PROFIT
        );

        // Act
        TransactionDto transaction = financeService.createTransaction(financeId, transactionDto);

        // Assert
        assertNotNull(transaction);
        assertEquals(TypeTransaction.PROFIT, transaction.typeTransaction());
    }

    @Test
    void createTransaction_shouldHandleMultipleTransactions() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of(1L)));
        CreateTransactionDto transactionDto1 = new CreateTransactionDto(BigDecimal.valueOf(1000), "Food", "Dinner", TypeTransaction.EXPENSE);
        CreateTransactionDto transactionDto2 = new CreateTransactionDto(BigDecimal.valueOf(1500), "Entertainment", "Concert", TypeTransaction.EXPENSE);

        // Act
        TransactionDto transaction1 = financeService.createTransaction(financeId, transactionDto1);
        TransactionDto transaction2 = financeService.createTransaction(financeId, transactionDto2);

        // Assert
        assertNotNull(transaction1);
        assertNotNull(transaction2);
        assertEquals(BigDecimal.valueOf(1000), transaction1.amount());
        assertEquals(BigDecimal.valueOf(1500), transaction2.amount());
    }

    @Test
    void filterTransactions_shouldReturnNoTransactionsWhenNoMatch() {
        // Arrange
        Long financeId = financeService.createEmptyFinance(new CreateFinanceDto(BigDecimal.valueOf(5000), BigDecimal.valueOf(10000), BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of(1L)));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(1000), "Food", "Lunch", TypeTransaction.EXPENSE));
        financeService.createTransaction(financeId, new CreateTransactionDto(BigDecimal.valueOf(500), "Entertainment", "Movie", TypeTransaction.EXPENSE));
        FilterTransactionDto filterDto = new FilterTransactionDto(Instant.now().minusSeconds(86400), Instant.now().plusSeconds(86400), "Travel", "EXPENSE");

        // Act
        List<TransactionDto> transactions = financeService.filterTransactions(financeId, filterDto);

        // Assert
        assertTrue(transactions.isEmpty());
    }

}