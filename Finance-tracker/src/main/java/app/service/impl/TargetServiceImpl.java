package app.service.impl;

import app.dto.transaction.TransactionDto;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.service.FinanceService;
import app.service.TargetService;
import app.service.UserService;
import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neket27.context.UserContext;
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
        app.entity.User user = (app.entity.User) UserContext.getCurrentUser();
        app.entity.Finance finance = findFinance(user.getEmail());

        Set<Transaction> transactions = financeService.list(financeId);
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.EXPENSE && t.getDate().isAfter(thirtyDaysAgo))
                .map(Transaction::getAmount)
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
        Finance finance = financeService.getFinanceById(financeId);
        BigDecimal current = finance.getCurrentSavings();
        BigDecimal goal = finance.getSavingsGoal();
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
        User user = (User) UserContext.getCurrentUser();
        Finance finance = financeService.findFinanceById(user.getFinanceId());
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("==== Финансовый отчет ====\n");
        reportBuilder.append("Текущие накопления: ").append(finance.getCurrentSavings()).append("\n");
        reportBuilder.append("Цель накопления: ").append(finance.getSavingsGoal()).append("\n");
        reportBuilder.append("Прогресс к цели: ").append(getProgressTowardsGoal(user.getFinanceId())).append("%\n");

        reportBuilder.append("Суммарный доход за период: ")
                .append(financeService.getTotalProfit(LocalDate.now().minusMonths(1), LocalDate.now(), user.getFinanceId())).append("\n");
        reportBuilder.append("Суммарные расходы за период: ")
                .append(financeService.getTotalExpenses(LocalDate.now().minusMonths(1), LocalDate.now(), user.getFinanceId())).append("\n");

        reportBuilder.append("Расходы по категориям:\n");
        financeService.getExpensesByCategory(((app.entity.User) UserContext.getCurrentUser()).getFinanceId()).forEach((category, total) ->
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
        app.entity.User user = (app.entity.User) UserContext.getCurrentUser();
        app.entity.Finance finance = findFinance(user.getEmail());
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
    private app.entity.Finance findFinance(String email) {
        User user = userService.getUserByEmail(email);
        return financeService.findFinanceById(user.getFinanceId());
    }
}
