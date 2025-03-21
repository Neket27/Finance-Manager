package test.integration;

import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.Transaction;
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
    void setup() {
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
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(1000.00),
                "Salary",
                Instant.now(),
                "Monthly salary",
                TypeTransaction.EXPENSE
        );

        TransactionDto transaction = transactionService.create(dto, user.financeId());

        assertNotNull(transaction);
        assertEquals("Salary", transaction.category());
    }

    @Test
    @Order(2)
    void testGetTransactionById() {
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(500.00),
                "Rent",
                Instant.now(),
                "Apartment rent",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        TransactionDto found = transactionService.getTransactionById(created.id());

        assertEquals(created.id(), found.id());
    }

    @Test
    @Order(3)
    void testEditTransaction() {
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(200.00),
                "Groceries",
                Instant.now(),
                "Weekly groceries",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        var updateDto = new UpdateTransactionDto(
                created.id(),
                BigDecimal.valueOf(250.00),
                "Groceries Updated",
                created.date(),
                "Updated groceries",
                TypeTransaction.EXPENSE
        );

        TransactionDto updated = transactionService.edit(updateDto);

        assertEquals("Groceries Updated", updated.category());
        assertEquals(0, updated.amount().compareTo(BigDecimal.valueOf(250.00)));
    }

    @Test
    @Order(4)
    void testEditTransactionNotFound() {
        var updateDto = new UpdateTransactionDto(
                99999L,
                BigDecimal.valueOf(300.00),
                "Non-existent",
                Instant.now(),
                "Should fail",
                TypeTransaction.EXPENSE
        );

        assertThrows(NotFoundException.class, () -> transactionService.edit(updateDto));
    }

    @Test
    @Order(5)
    void testDeleteTransaction() {
        var dto = new CreateTransactionDto(
                BigDecimal.valueOf(100.00),
                "Subscription",
                Instant.now(),
                "Monthly subscription",
                TypeTransaction.EXPENSE
        );
        TransactionDto created = transactionService.create(dto, user.financeId());

        boolean deleted = transactionService.delete(created.id());
        assertTrue(deleted);

        assertThrows(NotFoundException.class, () -> transactionService.getTransactionById(created.id()));
    }

    @Test
    @Order(6)
    void testDeleteTransactionNonExisting() {
        boolean deleted = transactionService.delete(99999L);
        assertFalse(deleted);
    }

    @Test
    @Order(7)
    void testFindAll() {
        var dto1 = new CreateTransactionDto(
                BigDecimal.valueOf(100.00), "Cat1", Instant.now(), "desc1", TypeTransaction.EXPENSE);
        var dto2 = new CreateTransactionDto(
                BigDecimal.valueOf(200.00), "Cat2", Instant.now(), "desc2", TypeTransaction.PROFIT);

        TransactionDto t1 = transactionService.create(dto1, user.financeId());
        TransactionDto t2 = transactionService.create(dto2, user.financeId());

        FinanceDto financeDto = FinanceDto.builder()
                .transactionsId(List.of(t1.id(), t2.id()))
                .build();

        List<TransactionDto> transactions = transactionService.findAll(financeDto);

        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().anyMatch(t -> t.id().equals(t1.id())));
        assertTrue(transactions.stream().anyMatch(t -> t.id().equals(t2.id())));
    }

    @Test
    @Order(8)
    void testGetFilteredTransactionsByCategory() {
        var dto1 = new CreateTransactionDto(
                BigDecimal.valueOf(50.00), "Food", Instant.now(), "Lunch", TypeTransaction.EXPENSE);
        var dto2 = new CreateTransactionDto(
                BigDecimal.valueOf(70.00), "Transport", Instant.now(), "Bus ticket", TypeTransaction.EXPENSE);

        transactionService.create(dto1, user.financeId());
        transactionService.create(dto2, user.financeId());

        var filtered = transactionService.getFilteredTransactions(
                user.financeId(), null, null, "Food", null
        );

        assertEquals(1, filtered.size());
        assertEquals("Food", filtered.get(0).category());
    }

    @Test
    @Order(9)
    void testGetFilteredTransactionsByType() {
        var dto1 = new CreateTransactionDto(
                BigDecimal.valueOf(300.00), "Bonus", Instant.now(), "Yearly bonus", TypeTransaction.PROFIT);
        var dto2 = new CreateTransactionDto(
                BigDecimal.valueOf(50.00), "Groceries", Instant.now(), "Fruits", TypeTransaction.EXPENSE);

        transactionService.create(dto1, user.financeId());
        transactionService.create(dto2, user.financeId());

        var filtered = transactionService.getFilteredTransactions(
                user.financeId(), null, null, null, TypeTransaction.PROFIT
        );

        assertEquals(1, filtered.size());
        assertEquals(TypeTransaction.PROFIT, filtered.get(0).typeTransaction());
    }

    @Test
    @Order(10)
    void testGetTransactionsByFinanceId() {
        transactionService.create(
                new CreateTransactionDto(BigDecimal.valueOf(40), "Misc", Instant.now(), "Misc desc", TypeTransaction.EXPENSE),
                user.financeId()
        );

        List<Transaction> transactions = transactionService.getTransactionsByFinanceId(user.financeId());

        assertFalse(transactions.isEmpty());
        assertEquals(user.financeId(), transactions.get(0).getFinanceId());
    }
}
