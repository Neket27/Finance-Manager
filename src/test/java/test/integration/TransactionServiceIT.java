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
                1000.0,
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
        assertEquals(1000.0, transaction.amount());
        assertEquals("Monthly salary", transaction.description());
    }

    @Test
    @Order(2)
    void testGetTransactionById() {
        // Arrange
        var dto = new CreateTransactionDto(
                500.0,
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
    }

    @Test
    @Order(3)
    void testEditTransaction() {
        // Arrange
        var dto = new CreateTransactionDto(
                200.0,
                "Bonus",
                Instant.now(),
                "Year-end bonus",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        var updateDto = new UpdateTransactionDto(
                created.id(),
                300.0,
                "Bonus Updated",
                Instant.now(),
                "Updated bonus description",
                TypeTransaction.EXPENSE
        );

        // Act
        TransactionDto updated = transactionService.edit(updateDto);

        // Assert
        assertEquals("Bonus Updated", updated.category());
        assertEquals(300.0, updated.amount());
        assertEquals("Updated bonus description", updated.description());
    }

    @Test
    @Order(4)
    void testDeleteTransaction() {
        // Arrange
        var dto = new CreateTransactionDto(
                700.0,
                "Freelance",
                Instant.now(),
                "Freelance project",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        // Act
        boolean deleted = transactionService.delete(created.id());

        // Assert
        assertTrue(deleted);
        assertThrows(NotFoundException.class, () -> transactionService.getTransactionById(created.id()));
    }

    @Test
    @Order(5)
    void testFindAllByFinanceDto() {
        // Arrange
        var tx1 = transactionService.create(new CreateTransactionDto(50.0, "Food", Instant.now(), "Groceries", TypeTransaction.EXPENSE), user.financeId());
        var tx2 = transactionService.create(new CreateTransactionDto(30.0, "Transport", Instant.now(), "Bus ticket", TypeTransaction.EXPENSE), user.financeId());
        var financeWithTx = new FinanceDto(user.financeId(), 0.0, 0.0, 0.0, 0.0, List.of(tx1.id(), tx2.id()));

        // Act
        var transactions = transactionService.findAll(financeWithTx);

        // Assert
        assertEquals(2, transactions.size());
    }

    @Test
    @Order(6)
    void testGetFilteredTransactions() {
        // Arrange
        transactionService.create(new CreateTransactionDto(150.0, "Clothes", Instant.now(), "Shopping spree", TypeTransaction.EXPENSE), user.financeId());
        transactionService.create(new CreateTransactionDto(200.0, "Gift", Instant.now(), "Birthday gift", TypeTransaction.EXPENSE), user.financeId());

        // Act
        List<TransactionDto> filtered = transactionService.getFilteredTransactions(
                user.financeId(),
                Instant.now().minusSeconds(86400),
                Instant.now().plusSeconds(86400),
                "Clothes",
                TypeTransaction.EXPENSE
        );

        // Assert
        assertEquals(1, filtered.size());
        assertEquals("Clothes", filtered.get(0).category());
    }
}
