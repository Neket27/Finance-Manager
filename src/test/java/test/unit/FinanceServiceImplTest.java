package test.unit;

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
import app.exception.EditException;
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
public class FinanceServiceImplTest {

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
                .id(1L)
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
        transaction = new Transaction.Builder()
                .id(1L)
                .amount(100)
                .category("Food")
                .typeTransaction(TypeTransaction.EXPENSE)
                .description("")
                .financeId(1L)
                .build();
    }

    @Test
    void addTransactionUserSuccess() {
        // Arrange
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.create(any(CreateTransactionDto.class), anyLong())).thenReturn(transactionDto);

        // Act
        TransactionDto result = financeService.addTransactionUser(new CreateTransactionDto(100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE));

        // Assert
        assertNotNull(result);
        verify(financeRepository, times(1)).save(finance);
    }

    @Test
    void checkExpenseLimit_SendNotificationIfExceeded() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        financeService.checkExpenseLimit("test@example.com");

        // Assert
        verify(notificationService, times(1)).sendMessage(eq("test@example.com"), contains("превысили"));
    }

    @Test
    void getProgressTowardsGoal_CorrectCalculation() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        double progress = financeService.getProgressTowardsGoal("test@example.com");

        // Assert
        assertEquals(120, progress);
    }


    @Test
    void filterTransactions_ReturnFilteredTransactions() {
        // Arrange
        when(transactionService.getFilteredTransactions(any(), any(), any(), any(), any())).thenReturn(List.of(transactionDto));

        // Act
        List<TransactionDto> transactions = financeService.filterTransactions(
                userDto.financeId(), Instant.now(), Instant.now(), "Food", TypeTransaction.EXPENSE, "test@example.com");

        // Assert
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionDto, transactions.get(0));
    }


    @Test
    void getExpensesByCategory_ReturnsCorrectGrouping() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionById(1L)).thenReturn(transactionDto);
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(2L, 50.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE));

        // Act
        Map<String, Double> expenses = financeService.getExpensesByCategory("test@example.com");

        // Assert
        assertEquals(1, expenses.size());
        assertEquals(150, expenses.get("Food"));
    }

    @Test
    void getTotalIncome_CorrectCalculation() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionById(1L)).thenReturn(new TransactionDto(3L, 200.0, "Salary", Instant.now(), "", TypeTransaction.PROFIT));
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(3L, 200.0, "Salary", Instant.now(), "", TypeTransaction.PROFIT));

        // Act
        double totalIncome = financeService.getTotalIncome(LocalDate.now().minusDays(30), LocalDate.now(), "test@example.com");

        // Assert
        assertEquals(400, totalIncome);
    }

    @Test
    void removeTransactionUserSuccess() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.delete(1L)).thenReturn(true);
        when(financeRepository.save(finance)).thenReturn(finance);

        // Act
        boolean result = financeService.removeTransactionUser(1L);

        // Assert
        assertTrue(result);
        verify(financeRepository, times(1)).save(finance);
    }

    @Test
    void editTransaction_Success() {
        // Arrange
        TransactionDto expectedTransactionDto = new TransactionDto.Builder()
                .id(1L)
                .amount(120)
                .build();
        when(transactionService.edit(any(UpdateTransactionDto.class))).thenReturn(expectedTransactionDto);

        // Act
        TransactionDto result = financeService.editTransaction(new UpdateTransactionDto(1L, 120.0, "Groceries", Instant.now(), "", TypeTransaction.EXPENSE));

        // Assert
        assertNotNull(result);
        assertEquals(120, result.amount());
    }

    @Test
    void editTransaction_ThrowsEditException() {
        // Arrange
        when(transactionService.edit(any(UpdateTransactionDto.class))).thenThrow(new RuntimeException());

        // Act & Assert
        assertThrows(EditException.class, () -> financeService.editTransaction(new UpdateTransactionDto(1L, 120.0, "Groceries", Instant.now(), "", TypeTransaction.EXPENSE)));
    }

    @Test
    void getTransactionUserReturnsTransactions() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(transactionService.getTransactionsByFinanceId(userDto.financeId())).thenReturn(List.of(transaction));
        when(transactionMapper.toDtoList(anyList())).thenReturn(List.of(transactionDto));

        // Act
        List<TransactionDto> transactions = financeService.getTransactions(userDto.email());

        // Assert
        assertEquals(1, transactions.size());
    }
}

