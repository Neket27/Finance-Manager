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
import app.exception.LimitAmountBalance;
import app.exception.NotFoundException;
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

import java.math.BigDecimal;
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
                .role(Role.USER)
                .build();

        finance = new Finance.Builder()
                .id(1L)
                .currentSavings(BigDecimal.valueOf(1000))
                .monthlyBudget(BigDecimal.valueOf(500))
                .totalExpenses(BigDecimal.valueOf(600))
                .transactionsId(new ArrayList<>(List.of(1L, 2L)))
                .build();

        UserContext.setCurrentUser(userDto);

        financeDto = new FinanceDto(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(2000), List.of(1L, 2L));
        transactionDto = new TransactionDto(1L, BigDecimal.valueOf(400), "Food", Instant.now(), "", TypeTransaction.EXPENSE,1L);
        transaction = new Transaction.Builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
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
        when(userService.getUserByEmail(any())).thenReturn(userDto);
        // Act
        TransactionDto result = financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(100), "Food", Instant.now(), "", TypeTransaction.EXPENSE));

        // Assert
        assertNotNull(result);
        verify(financeRepository, times(1)).save(finance);
    }

    @Test
    void checkExpenseLimit_SendNotificationIfExceeded() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionsByFinanceId(finance.getId())).thenReturn(List.of(transaction,transaction));
        when(transactionService.getTransactionById(any())).thenReturn(transactionDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);

        // Act
       boolean isExpenseLimit =  financeService.checkMonthlyExpenseLimit("test@example.com");

        // Assert
        assertTrue(isExpenseLimit);
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
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(2L, BigDecimal.valueOf(50), "Food", Instant.now(), "", TypeTransaction.EXPENSE,1L));

        // Act
        Map<String, BigDecimal> expenses = financeService.getExpensesByCategory("test@example.com");

        // Assert
        assertEquals(1, expenses.size());
        assertEquals(BigDecimal.valueOf(450), expenses.get("Food"));
    }

    @Test
    void getTotalIncome_CorrectCalculation() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionById(1L)).thenReturn(new TransactionDto(3L, BigDecimal.valueOf(200), "Salary", Instant.now(), "", TypeTransaction.PROFIT, 1L));
        when(transactionService.getTransactionById(2L)).thenReturn(new TransactionDto(3L, BigDecimal.valueOf(200), "Salary", Instant.now(), "", TypeTransaction.PROFIT, 1L));

        // Act
        BigDecimal totalIncome = financeService.getTotalProfit(LocalDate.now().minusDays(30), LocalDate.now(), "test@example.com");

        // Assert
        assertEquals(BigDecimal.valueOf(400), totalIncome);
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
                .amount(BigDecimal.valueOf(120))
                .build();
        when(transactionService.edit(any(UpdateTransactionDto.class))).thenReturn(expectedTransactionDto);

        // Act
        TransactionDto result = financeService.editTransaction(new UpdateTransactionDto(1L, BigDecimal.valueOf(120), "Groceries", Instant.now(), "", TypeTransaction.EXPENSE));

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(120), result.amount());
    }

    @Test
    void editTransaction_ThrowsEditException() {
        // Arrange
        when(transactionService.edit(any(UpdateTransactionDto.class))).thenThrow(new RuntimeException());

        // Act & Assert
        assertThrows(EditException.class, () -> financeService.editTransaction(new UpdateTransactionDto(1L, BigDecimal.valueOf(120), "Groceries", Instant.now(), "", TypeTransaction.EXPENSE)));
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

    @Test
    void addTransactionUser_ThrowsLimitAmountBalance_WhenExpenseExceedsBalance() {
        // Arrange
        finance.setCurrentSavings(BigDecimal.valueOf(50));
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        when(userService.getUserByEmail(any())).thenReturn(userDto);


        // Act & Assert
        LimitAmountBalance exception = assertThrows(LimitAmountBalance.class, () -> {
            financeService.addTransactionUser(new CreateTransactionDto(BigDecimal.valueOf(100), "Food", Instant.now(), "", TypeTransaction.EXPENSE));
        });

        assertEquals("Amount translation limit exceeded", exception.getMessage());
    }

    @Test
    void addTransactionUser_AddsIncomeSuccessfully() {
        // Arrange
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));
        CreateTransactionDto incomeTransaction = new CreateTransactionDto(BigDecimal.valueOf(200), "Salary", Instant.now(), "", TypeTransaction.PROFIT);
        TransactionDto incomeDto = new TransactionDto(5L, BigDecimal.valueOf(200), "Salary", Instant.now(), "", TypeTransaction.PROFIT,1L);
        when(transactionService.create(any(CreateTransactionDto.class), anyLong())).thenReturn(incomeDto);

        // Act
        TransactionDto result = financeService.addTransactionUser(incomeTransaction);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.id());
        assertEquals(BigDecimal.valueOf(1200), finance.getCurrentSavings());
        verify(financeRepository).save(finance);
    }

    @Test
    void transactionAmountExceedsBalance_ReturnsTrue_WhenInsufficientFunds() {
        // Arrange
        finance.setCurrentSavings(BigDecimal.valueOf(50));
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        boolean result = financeService.transactionAmountExceedsBalance("test@example.com", BigDecimal.valueOf(100));

        // Assert
        assertTrue(result);
    }

    @Test
    void transactionAmountExceedsBalance_ReturnsFalse_WhenSufficientFunds() {
        // Arrange
        finance.setCurrentSavings(BigDecimal.valueOf(500));
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        boolean result = financeService.transactionAmountExceedsBalance("test@example.com", BigDecimal.valueOf(100));

        // Assert
        assertFalse(result);
    }

    @Test
    void getProgressTowardsGoal_ReturnsZero_WhenGoalIsZero() {
        // Arrange
        financeDto = new FinanceDto(1L, BigDecimal.valueOf(500), BigDecimal.valueOf(0), BigDecimal.valueOf(600), BigDecimal.ZERO, List.of(1L, 2L));
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        double result = financeService.getProgressTowardsGoal("test@example.com");

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getProgressTowardsGoal_Returns100Percent_WhenCurrentEqualsGoal() {
        // Arrange
        financeDto = new FinanceDto(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(1000), List.of(1L, 2L));
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(financeMapper.toDto(finance)).thenReturn(financeDto);
        when(financeRepository.findById(1L)).thenReturn(Optional.of(finance));

        // Act
        double result = financeService.getProgressTowardsGoal("test@example.com");

        // Assert
        assertEquals(120.0, result);
    }

    @Test
    void removeTransactionUser_ReturnsFalse_WhenTransactionDeleteFails() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(transactionService.delete(1L)).thenThrow(new RuntimeException("DB error"));

        // Act
        boolean result = financeService.removeTransactionUser(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void getFinanceById_WhenFinanceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(financeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> financeService.getFinanceById(1L));
        assertEquals("Finance not found with id: 1", ex.getMessage());
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // Act
        financeService.save(finance);

        // Assert
        verify(financeRepository, times(1)).save(finance);
    }

}
