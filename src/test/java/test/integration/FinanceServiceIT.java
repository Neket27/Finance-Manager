package test.integration;

import app.dto.finance.CreateFinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.entity.*;
import app.exception.common.CreateException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.impl.FinanceServiceImpl;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceIT {

    private FinanceService financeService;
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
        financeService = new FinanceServiceImpl(new FinanceJdbcRepository(database.jdbcTemplate()), transactionService, Mappers.getMapper(FinanceMapper.class));
    }


    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
    }

    @Test
    void createTransaction_shouldAddTransactionSuccessfully() {
        // Arrange,

        Finance finance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(3000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(finance);

        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Groceries shopping")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        // Act
        Transaction transaction = financeService.createTransaction(financeId, createTransaction);

        // Assert
        assertNotNull(transaction);
        assertEquals(BigDecimal.valueOf(1000), transaction.getAmount());
        assertEquals("Food", transaction.getCategory());
        assertEquals("Groceries shopping", transaction.getDescription());
    }

    @Test
    void createTransaction_shouldThrowExceptionWhenInsufficientFunds() {
        // Arrange
        Finance finance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(500))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();


        Long financeId = financeService.createEmptyFinance(finance);
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(2000))
                .category("Rent")
                .description("Monthly rent payment")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();


        // Act & Assert
        assertThrows(CreateException.class, () -> financeService.createTransaction(financeId, transaction));
    }

    @Test
    void getExpensesByCategory_shouldReturnCorrectExpenses() {
        // Arrange
        Finance createFinance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(3000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(createFinance);

        Transaction lunchTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Transaction movieTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .category("Entertainment")
                .description("Movie")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Transaction snacksTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(200))
                .category("Food")
                .description("Snacks")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        financeService.createTransaction(financeId, lunchTransaction);
        financeService.createTransaction(financeId, movieTransaction);
        financeService.createTransaction(financeId, snacksTransaction);

        // Act
        Map<String, BigDecimal> expenses = financeService.getExpensesByCategory(financeId);

        // Assert
        assertEquals(BigDecimal.valueOf(1200).stripTrailingZeros(), expenses.get("Food").stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(500).stripTrailingZeros(), expenses.get("Entertainment").stripTrailingZeros());
    }

    @Test
    void filterTransactions_shouldReturnCorrectTransactions() {
        Finance createFinance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(3000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(createFinance);

        Transaction lunchTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Transaction movieTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .category("Entertainment")
                .description("Movie")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Transaction snacksTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(200))
                .category("Food")
                .description("Snacks")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        financeService.createTransaction(financeId, lunchTransaction);
        financeService.createTransaction(financeId, movieTransaction);

        FilterTransactionDto filterDto = FilterTransactionDto.builder()
                .startDate(Instant.now().minusSeconds(86400))
                .endDate(Instant.now().plusSeconds(86400))
                .category("Food")
                .typeTransaction("EXPENSE")
                .build();

        // Act
        List<TransactionDto> transactions = financeService.filterTransactions(financeId, filterDto);

        // Assert
        assertEquals(1, transactions.size());
        assertEquals("Food", transactions.get(0).category());
    }

    @Test
    void createTransaction_shouldReturnCorrectTransactionType() {
        // Arrange
        Finance createFinance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(1000))
                .savingsGoal(BigDecimal.valueOf(5000))
                .currentSavings(BigDecimal.valueOf(1000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(createFinance);


        Transaction createTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        // Act
        Transaction transaction = financeService.createTransaction(financeId, createTransaction);

        // Assert
        assertNotNull(transaction);
        assertEquals(TypeTransaction.PROFIT, transaction.getTypeTransaction());
    }

    @Test
    void createTransaction_shouldHandleMultipleTransactions() {
        // Arrange
        Finance createFinance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(3000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(createFinance);

        Transaction t1 = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Dinner")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now()) // или нужная дата
                .financeId(financeId) // передай актуальный ID, если есть
                .build();

        Transaction t2 = Transaction.builder()
                .amount(BigDecimal.valueOf(1500))
                .category("Entertainment")
                .description("Concert")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now())
                .financeId(financeId)
                .build();

        // Act
        Transaction transaction1 = financeService.createTransaction(financeId, t1);
        Transaction transaction2 = financeService.createTransaction(financeId, t2);

        // Assert
        assertNotNull(transaction1);
        assertNotNull(transaction2);
        assertEquals(BigDecimal.valueOf(1000), transaction1.getAmount());
        assertEquals(BigDecimal.valueOf(1500), transaction2.getAmount());
    }

    @Test
    void filterTransactions_shouldReturnNoTransactionsWhenNoMatch() {
        // Arrange
        Finance createFinance = Finance.builder()
                .monthlyBudget(BigDecimal.valueOf(5000))
                .savingsGoal(BigDecimal.valueOf(10000))
                .currentSavings(BigDecimal.valueOf(3000))
                .totalExpenses(BigDecimal.ZERO)
                .transactionsId(List.of(1L))
                .build();

        Long financeId = financeService.createEmptyFinance(createFinance);

        Transaction lunchTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now()) // или конкретная дата, если нужно
                .financeId(financeId)
                .build();

        Transaction movieTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .category("Entertainment")
                .description("Movie")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now())
                .financeId(financeId)
                .build();


        financeService.createTransaction(financeId, lunchTransaction);
        financeService.createTransaction(financeId, movieTransaction);

        FilterTransactionDto filterDto = FilterTransactionDto.builder()
                .startDate(Instant.now().minusSeconds(86400))
                .endDate(Instant.now().plusSeconds(86400))
                .category("Travel")
                .typeTransaction("EXPENSE")
                .build();


        // Act
        List<TransactionDto> transactions = financeService.filterTransactions(financeId, filterDto);

        // Assert
        assertTrue(transactions.isEmpty());
    }

}