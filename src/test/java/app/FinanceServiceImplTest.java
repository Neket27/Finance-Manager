package app;

import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exeption.EditException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.repository.FinanceRepository;
import app.service.NotificationService;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.FinanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceImplTest {

    @Mock
    private FinanceRepository financeRepository;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private FinanceMapper financeMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FinanceServiceImpl financeService;

    private UserDto userDto;
    private Finance finance;
    private FinanceDto financeDto;
    private TransactionDto transactionDto;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        userDto = new UserDto.Builder()
                .name("name")
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .finance(1L)
                .role(Role.User)
                .build();

        finance = new Finance.Builder()
                .id(1L)
                .currentSavings(1000)
                .monthlyBudget(500)
                .totalExpenses(600)
                .transactionsId(new ArrayList<>(List.of(1L, 2L)))
                .build();

        UserContext.setCurrentUser(userDto);

        financeDto = new FinanceDto(1L, 1000.0, 500.0, 600.0, 2000.0, List.of(1L, 2L));
        transactionDto = new TransactionDto(1L, 100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE);
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(100);
        transaction.setCategory("Food");
        transaction.setTypeTransaction(TypeTransaction.EXPENSE);
    }

    @Test
    void addTransactionUser_Success() {
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.create(any(CreateTransactionDto.class))).thenReturn(transactionDto);

        TransactionDto result = financeService.addTransactionUser(new CreateTransactionDto(100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE));

        assertNotNull(result);
        verify(financeRepository, times(1)).save(finance);
    }

    @Test
    void checkExpenseLimit_SendNotificationIfExceeded() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        financeService.checkExpenseLimit("test@example.com");

        verify(notificationService, times(1)).sendMessage(eq("test@example.com"), contains("превысили"));
    }

    @Test
    void getProgressTowardsGoal_CorrectCalculation() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        double progress = financeService.getProgressTowardsGoal("test@example.com");

        assertEquals(120, progress);
    }

    @Test
    void filterTransactions_ReturnFilteredTransactions() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getFilteredTransactions(any(), any(), any(), any(), any())).thenReturn(List.of(transactionDto));

        List<TransactionDto> transactions = financeService.filterTransactions(Instant.now(), Instant.now(), "Food", TypeTransaction.EXPENSE, "test@example.com");

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionDto, transactions.get(0));
    }

    @Test
    void getExpensesByCategory_ReturnsCorrectGrouping() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionById(1L)).thenReturn(transactionDto);
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(2L, 50.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE));

        Map<String, Double> expenses = financeService.getExpensesByCategory("test@example.com");

        assertEquals(1, expenses.size());
        assertEquals(150, expenses.get("Food"));
    }

    @Test
    void getTotalIncome_CorrectCalculation() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        when(transactionService.getTransactionById(1L)).thenReturn(new TransactionDto(3L, 200.0, "Salary", Instant.now(), "", TypeTransaction.PROFIT));
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(3L, 200.0, "Salary", Instant.now(), "", TypeTransaction.PROFIT));

        double totalIncome = financeService.getTotalIncome(LocalDate.now().minusDays(30), LocalDate.now(), "test@example.com");

        assertEquals(400, totalIncome);
    }

    @Test
    void removeTransactionUser_Success() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.delete(1L)).thenReturn(true);
        when(financeRepository.save(finance)).thenReturn(finance);

        boolean result = financeService.removeTransactionUser(1L);

        assertTrue(result);
        verify(financeRepository, times(1)).save(finance);
    }

    @Test
    void editTransaction_Success() {

        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(120);

        TransactionDto transactionDto = new TransactionDto.Builder()
                .id(1L)
                .amount(120)
                .build();

        when(transactionService.edit(any(UpdateTransactionDto.class))).thenReturn(t);
        when(transactionMapper.toDto(t)).thenReturn(transactionDto);

        TransactionDto result = financeService.editTransaction(new UpdateTransactionDto(1L, 120.0, "Groceries", Instant.now(), "", TypeTransaction.EXPENSE));

        assertNotNull(result);
        assertEquals(120, result.amount());
    }

    @Test
    void editTransaction_ThrowsEditException() {
        when(transactionService.edit(any(UpdateTransactionDto.class))).thenThrow(new RuntimeException());

        assertThrows(EditException.class, () -> financeService.editTransaction(new UpdateTransactionDto(1L, 120.0, "Groceries", Instant.now(), "", TypeTransaction.EXPENSE)));
    }

    @Test
    void getTransactionUser_ReturnsTransactions() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        List<TransactionDto> transactions = financeService.getTransactions("test@example.com");

        assertEquals(2, transactions.size());
    }
}