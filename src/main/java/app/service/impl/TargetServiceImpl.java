package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.TypeTransaction;
import app.service.FinanceService;
import app.service.TargetService;
import app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Реализация сервиса управления финансовыми целями пользователя.
 */

@Slf4j
@Service
@CustomLogging
@RequiredArgsConstructor
public class TargetServiceImpl implements TargetService {

    private final UserService userService;
    private final FinanceService financeService;

    /**
     * Устанавливает месячный бюджет пользователя.
     *
     * @param budget сумма месячного бюджета
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void updateMonthlyBudget(Long financeId, BigDecimal budget) {
        financeService.updatetMonthlyBudget(financeId, budget);
        log.debug("Месячный бюджет установлен: {}", budget);
    }

    @Override
    @Auditable
    @Transactional
    public Boolean isMonthBudgetExceeded(Long financeId) {
        Finance finance = findFinance(UserContext.getCurrentUser().email());

        Set<TransactionDto> transactions = financeService.list(financeId);
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE && t.date().isAfter(thirtyDaysAgo))
                .map(TransactionDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyBudget = finance.getMonthlyBudget();

        if (totalExpenses.compareTo(monthlyBudget) > 0) {
            System.out.println("Внимание! Вы превысили месячный бюджет на " + totalExpenses.subtract(monthlyBudget) + "!");
            return true;
        }

        return false;
    }

    @Override
    @Auditable
    @Transactional
    public Double getProgressTowardsGoal(Long financeId) {
        FinanceDto finance = financeService.getFinanceById(financeId);
        BigDecimal current = finance.currentSavings();
        BigDecimal goal = finance.savingsGoal();
        if (goal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return current.divide(goal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * Генерирует финансовый отчет пользователя.
     *
     * @return строковое представление финансового отчета
     */
    @Override
    @Auditable
    @Transactional
    public String generateFinancialReport() {
        UserDto user = userService.getUserByEmail(UserContext.getCurrentUser().email());
        Finance finance = financeService.findFinanceById(user.financeId());
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("==== Финансовый отчет ====\n");
        reportBuilder.append("Текущие накопления: ").append(finance.getCurrentSavings()).append("\n");
        reportBuilder.append("Цель накопления: ").append(finance.getSavingsGoal()).append("\n");
        reportBuilder.append("Прогресс к цели: ").append(getProgressTowardsGoal(user.id())).append("%\n");

        reportBuilder.append("Суммарный доход за период: ")
                .append(financeService.getTotalProfit(LocalDate.now().minusMonths(1), LocalDate.now(), user.id())).append("\n");
        reportBuilder.append("Суммарные расходы за период: ")
                .append(financeService.getTotalExpenses(LocalDate.now().minusMonths(1), LocalDate.now(), user.id())).append("\n");

        reportBuilder.append("Расходы по категориям:\n");
        financeService.getExpensesByCategory(UserContext.getCurrentUser().id()).forEach((category, total) ->
                reportBuilder.append(category).append(": ").append(total).append("\n"));

        reportBuilder.append("===========================\n");

        return reportBuilder.toString();
    }

    /**
     * Устанавливает цель накоплений пользователя.
     *
     * @param savingGoal сумма целевых накоплений
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void updateGoalSavings(BigDecimal savingGoal) {
        Finance finance = findFinance(UserContext.getCurrentUser().email());
        finance.setSavingsGoal(savingGoal);
        financeService.save(finance);
        log.debug("Цель накопления установлена: {}", savingGoal);
    }

    /**
     * Находит финансовые данные пользователя по электронной почте.
     *
     * @param email электронная почта пользователя
     * @return объект Finance, содержащий финансовые данные пользователя
     */
    private Finance findFinance(String email) {
        UserDto user = userService.getUserByEmail(email);
        return financeService.findFinanceById(user.financeId());
    }
}
