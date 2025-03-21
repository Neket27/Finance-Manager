package test.integration;

import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.TypeTransaction;
import app.exception.NotFoundException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.TransactionRepository;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.TransactionServiceImpl;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import test.db.TestDatabase;
import test.db.TestDatabaseFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionServiceIT {

    private TestDatabase database;
    private TransactionService transactionService;
    private TransactionRepository transactionRepository;
    private UserService userService;
    private UserDto user;

    @BeforeEach
    void setup(){

       database = TestDatabaseFactory.create();

        var financeRepository = new FinanceJdbcRepository(database.connection());
        var financeMapper = new FinanceMapper();
        var userMapper = new UserMapper();
        var userRepository = new UserJdbcRepository(database.connection());
        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
        user = userService.createUser(new CreateUserDto("Clark Kent", "clark@example.com", "password"));
        transactionRepository = new TransactionJdbcRepository(database.connection());
        var transactionMapper = new TransactionMapper();

        transactionService = new TransactionServiceImpl(transactionRepository, transactionMapper);
    }

    @AfterEach
    void tearDown() {
        TestDatabaseFactory.reset();
        UserContext.clear();
    }

    @Test
    @Order(1)
    void testCreateTransaction() {
        // Arrange
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(1000.00),
                "Salary",
                Instant.now(),
                "Monthly salary",
                TypeTransaction.EXPENSE
        );

        // Act
        TransactionDto transaction = transactionService.create(dto, user.financeId());

        // Assert
        assertNotNull(transaction);
        assertEquals("Salary", transaction.category());
        assertEquals(TypeTransaction.EXPENSE, transaction.typeTransaction());
        assertEquals(0, transaction.amount().compareTo(BigDecimal.valueOf(1000.00)));
        assertEquals("Monthly salary", transaction.description());
    }

    @Test
    @Order(2)
    void testGetTransactionById() {
        // Arrange
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(500.00),
                "Rent",
                Instant.now(),
                "Apartment rent",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        // Act
        TransactionDto found = transactionService.getTransactionById(created.id());

        // Assert
        assertEquals(created.id(), found.id());
        assertEquals("Rent", found.category());
        assertEquals("Apartment rent", found.description());
        assertEquals(0, found.amount().compareTo(BigDecimal.valueOf(500.00)));
    }

}
