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
                .currentSavings(5000.0)
                .savingsGoal(10000.0)
                .monthlyBudget(3000.0)
                .build();

        when(userService.getUserByEmail(any())).thenReturn(userDto);
        when(financeService.findFinanceById(any())).thenReturn(finance);
    }

    @Test
    void setMonthlyBudget_ShouldUpdateBudgetAndSave() {
        double newBudget = 4000.0;

        targetService.setMonthlyBudget(newBudget);

        assertEquals(newBudget, finance.getMonthlyBudget());
        verify(financeService, times(1)).save(finance);
    }

    @Test
    void checkBudgetExceeded_ShouldNotThrow_WhenNotExceeded() {
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, 500.0, "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE),
                new TransactionDto(2L, 1000.0, "k2", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE)
        );
        when(financeService.getTransactions(any())).thenReturn(transactions);

        assertDoesNotThrow(() -> targetService.checkBudgetExceeded(userDto.email()));
    }

    @Test
    void checkBudgetExceeded_ShouldNotThrow_WhenExceeded() {
        List<TransactionDto> transactions = List.of(
                new TransactionDto(1L, 2000.0, "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE),
                new TransactionDto(2L, 2000.0, "k1", Instant.now().minus(Duration.ofDays(10)), "", TypeTransaction.EXPENSE)
        );
        when(financeService.getTransactions(any())).thenReturn(transactions);

        assertDoesNotThrow(() -> targetService.checkBudgetExceeded(userDto.email()));
    }

    @Test
    void generateFinancialReport_ShouldContainExpectedValues() {
        when(financeService.getProgressTowardsGoal(any())).thenReturn(50.0);
        when(financeService.getTotalIncome(any(), any(), any())).thenReturn(8000.0);
        when(financeService.getTotalExpenses(any(), any(), any())).thenReturn(3000.0);
        when(financeService.getExpensesByCategory(any())).thenReturn(Map.of("Food", 1000.0, "Transport", 500.0));

        String report = targetService.generateFinancialReport();

        assertTrue(report.contains("Текущие накопления: 5000.0"));
        assertTrue(report.contains("Цель накопления: 10000.0"));
        assertTrue(report.contains("Прогресс к цели: 50.0%"));
        assertTrue(report.contains("Суммарный доход за период: 8000.0"));
        assertTrue(report.contains("Суммарные расходы за период: 3000.0"));
        assertTrue(report.contains("Food: 1000.0"));
        assertTrue(report.contains("Transport: 500.0"));
    }

    @Test
    void updateGoalSavings_ShouldUpdateGoalAndSave() {
        double newGoal = 15000.0;

        targetService.updateGoalSavings(newGoal);

        assertEquals(newGoal, finance.getSavingsGoal());
        verify(financeService, times(1)).save(finance);
    }
}
