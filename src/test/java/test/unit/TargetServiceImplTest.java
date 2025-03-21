package test.unit;

import app.context.UserContext;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.TypeTransaction;
import app.service.FinanceService;
import app.service.UserService;
import app.service.impl.TargetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private FinanceService financeService;

    @InjectMocks
    private TargetServiceImpl targetService;

    @Mock
    private UserContext userContext;

    private Finance finance;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto.Builder()
                .email("test@example.com")
                .password("newPassword123")
                .build();

        UserContext.setCurrentUser(userDto);

        finance = new Finance.Builder()
                .currentSavings(new BigDecimal("5000.00"))
                .savingsGoal(new BigDecimal("10000.00"))
                .monthlyBudget(new BigDecimal("3000.00"))
                .build();

        when(userService.getUserByEmail(any())).thenReturn(userDto);
        when(financeService.findFinanceById(any())).thenReturn(finance);
    }

    @Test
    void setMonthlyBudget_ShouldUpdateBudgetAndSave() {
        BigDecimal newBudget = new BigDecimal("4000.00");

        targetService.setMonthlyBudget(newBudget);

        assertEquals(newBudget, finance.getMonthlyBudget());
        verify(financeService, times(1)).save(finance);
    }

    @Test
    void checkBudgetExceeded_ShouldNotThrow_WhenNotExceeded() {
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, new BigDecimal("500.00"), "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE),
                new TransactionDto(2L, new BigDecimal("1000.00"), "k2", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE)
        );
        when(financeService.getTransactions(any())).thenReturn(transactions);

        assertDoesNotThrow(() -> targetService.isMonthBudgetExceeded(userDto.email()));
    }

    @Test
    void checkBudgetExceeded_ShouldNotThrow_WhenExceeded() {
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, new BigDecimal("2000.00"), "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE),
                new TransactionDto(2L, new BigDecimal("2000.00"), "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE)
        );
        when(financeService.getTransactions(any())).thenReturn(transactions);

        assertDoesNotThrow(() -> targetService.isMonthBudgetExceeded(userDto.email()));
    }

    @Test
    void generateFinancialReport_ShouldContainExpectedValues() {
        when(financeService.getProgressTowardsGoal(any())).thenReturn(50.00);
        when(financeService.getTotalProfit(any(), any(), any())).thenReturn(new BigDecimal("8000.00"));
        when(financeService.getTotalExpenses(any(), any(), any())).thenReturn(new BigDecimal("3000.00"));
        when(financeService.getExpensesByCategory(any())).thenReturn(Map.of("Food", new BigDecimal("1000.00"), "Transport", new BigDecimal("500.00")));

        String report = targetService.generateFinancialReport();

        assertTrue(report.contains("Текущие накопления: 5000.00"));
        assertTrue(report.contains("Цель накопления: 10000.00"));
        assertTrue(report.contains("Прогресс к цели: 50.00%"));
        assertTrue(report.contains("Суммарный доход за период: 8000.00"));
        assertTrue(report.contains("Суммарные расходы за период: 3000.00"));
        assertTrue(report.contains("Food: 1000.00"));
        assertTrue(report.contains("Transport: 500.00"));
    }

    @Test
    void updateGoalSavings_ShouldUpdateGoalAndSave() {
        BigDecimal newGoal = new BigDecimal("15000.00");

        targetService.updateGoalSavings(newGoal);

        assertEquals(newGoal, finance.getSavingsGoal());
        verify(financeService, times(1)).save(finance);
    }
}
