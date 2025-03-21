package test.integration;

import app.context.UserContext;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.TypeTransaction;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.TransactionRepository;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.*;
import app.service.impl.FinanceServiceImpl;
import app.service.impl.TargetServiceImpl;
import app.service.impl.TransactionServiceImpl;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import test.db.TestDatabase;
import test.db.TestDatabaseFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FinanceServiceIT {

    private static TestDatabase database;
    private static UserService userService;
    private TransactionService transactionService;
    private TargetService targetService;
    private TransactionRepository transactionRepository;
    private static FinanceService financeService;
    private UserDto user;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setup() {

        database = TestDatabaseFactory.create();

        UserMapper userMapper = new UserMapper();
        UserJdbcRepository userRepository = new UserJdbcRepository(database.connection());
        FinanceRepository financeRepository = new FinanceJdbcRepository(database.connection());
        FinanceMapper financeMapper = new FinanceMapper();
        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
        user = userService.createUser(new CreateUserDto("Clark Kent", "clark@example.com", "password"));
        UserContext.setCurrentUser(user);
        notificationService = mock(NotificationService.class);
        transactionRepository = new TransactionJdbcRepository(database.connection());
        TransactionMapper transactionMapper = new TransactionMapper();
        transactionService = new TransactionServiceImpl(transactionRepository, transactionMapper);
        financeService = new FinanceServiceImpl(financeRepository, userService, transactionService, financeMapper, transactionMapper, notificationService);
        targetService = new TargetServiceImpl(userService, financeService);
    }

    @AfterEach
    void tearDown() {
        TestDatabaseFactory.reset();
    }

    @Test
    void testAddTransactionUser() {
        // Arrange
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(BigDecimal.valueOf(500.0), "Salary", Instant.now(), "Monthly salary", TypeTransaction.PROFIT);

        // Act
        TransactionDto transactionDto = financeService.addTransactionUser(createTransactionDto);

        // Assert
        assertNotNull(transactionDto);
        assertEquals("Salary", transactionDto.category());
        assertEquals(BigDecimal.valueOf(500.0), transactionDto.amount());
    }

    @Test
    void testCheckExpenseMonthLimit() {
        // Arrange
        targetService.setMonthlyBudget(BigDecimal.valueOf(100.0));
        financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(500.0), "auto", Instant.now(), "", TypeTransaction.PROFIT));
        financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(200.0), "auto", Instant.now(), "", TypeTransaction.EXPENSE));

        // Act
        boolean isExpenseLimit = financeService.checkMonthlyExpenseLimit(user.email());

        // Assert
        assertTrue(isExpenseLimit);
    }

    @Test
    void testGetProgressTowardsGoal() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(50.0), "auto", Instant.now(), "", TypeTransaction.PROFIT));
        targetService.updateGoalSavings(BigDecimal.valueOf(140.0));

        // Act
        double progress = financeService.getProgressTowardsGoal("clark@example.com");

        // Assert
        assertEquals(35, (int) progress);
    }

    @Test
    void testRemoveTransactionUser() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(50.0), "auto", Instant.now(), "", TypeTransaction.PROFIT));

        // Act
        boolean result = financeService.removeTransactionUser(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetTotalIncome() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(1000.11), "auto", Instant.now(), "", TypeTransaction.PROFIT));

        // Act
        BigDecimal totalIncome = financeService.getTotalProfit(LocalDate.now(), LocalDate.now(), user.email());

        // Assert
        assertEquals(BigDecimal.valueOf(1000.11), totalIncome);
    }
}
