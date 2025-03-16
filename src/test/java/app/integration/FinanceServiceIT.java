package app.integration;

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
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FinanceServiceIT {

    private static PostgreSQLContainer<?> postgres;
    private static UserService userService;
    private TransactionService transactionService;
    private TargetService targetService;
    private TransactionRepository transactionRepository;
    private static FinanceService financeService;
    private FinanceRepository financeRepository;
    private UserDto user;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setup() throws Exception {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);
        postgres.start();

        var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(
                "db/changelog/changelog-master.yml",
                new ClassLoaderResourceAccessor(),
                database
        );
        liquibase.update();
        UserMapper userMapper = new UserMapper();
        UserJdbcRepository userRepository = new UserJdbcRepository(connection);
        FinanceRepository financeRepository = new FinanceJdbcRepository(connection);
        FinanceMapper financeMapper = new FinanceMapper();
        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
        user = userService.createUser(new CreateUserDto("Clark Kent", "clark@example.com", "password"));
        UserContext.setCurrentUser(user);
        notificationService = mock(NotificationService.class);
        transactionRepository = new TransactionJdbcRepository(connection);
        TransactionMapper transactionMapper = new TransactionMapper();
        transactionService = new TransactionServiceImpl(transactionRepository, transactionMapper);
        financeService = new FinanceServiceImpl(financeRepository, userService, transactionService, financeMapper, transactionMapper, notificationService);
        targetService = new TargetServiceImpl(userService, financeService);
        this.financeRepository = financeRepository;
    }

    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAfterClass() {
        postgres.stop();
    }

    @Test
    void testAddTransactionUser() {
        // Arrange
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(500.0, "Salary", Instant.now(), "Monthly salary", TypeTransaction.PROFIT);

        // Act
        TransactionDto transactionDto = financeService.addTransactionUser(createTransactionDto);

        // Assert
        assertNotNull(transactionDto);
        assertEquals("Salary", transactionDto.category());
        assertEquals(500.0, transactionDto.amount());
    }

    @Test
    void testCheckExpenseLimit() {
        // Arrange
        targetService.setMonthlyBudget(100);
        targetService.updateGoalSavings(50);
        financeService.addTransactionUser(new CreateTransactionDto(500.0, "auto", Instant.now(), "", TypeTransaction.PROFIT));
        financeService.addTransactionUser(new CreateTransactionDto(200.0, "auto", Instant.now(), "", TypeTransaction.EXPENSE));

        // Act
        targetService.checkBudgetExceeded(UserContext.getCurrentUser().email());
        boolean isExpenseLimit = financeService.checkExpenseLimit(user.email());

        // Assert
        assertFalse(isExpenseLimit);
    }

    @Test
    void testGetProgressTowardsGoal() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(50.0, "auto", Instant.now(), "", TypeTransaction.PROFIT));
        targetService.updateGoalSavings(140);

        // Act
        double progress = financeService.getProgressTowardsGoal("clark@example.com");

        // Assert
        assertEquals(35, (int) progress);
    }

    @Test
    void testRemoveTransactionUser() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(50.0, "auto", Instant.now(), "", TypeTransaction.PROFIT));

        // Act
        boolean result = financeService.removeTransactionUser(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetTotalIncome() {
        // Arrange
        financeService.addTransactionUser(new CreateTransactionDto(1000.0, "auto", Instant.now(), "", TypeTransaction.PROFIT));

        // Act
        double totalIncome = financeService.getTotalIncome(LocalDate.now(), LocalDate.now(), user.email());

        // Assert
        assertEquals(1000.0, totalIncome);
    }
}
