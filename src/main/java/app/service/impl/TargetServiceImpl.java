package app.service.impl;

import app.context.UserContext;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.TypeTransaction;
import app.service.FinanceService;
import app.service.TargetService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class TargetServiceImpl implements TargetService {

    private final Logger log = LoggerFactory.getLogger(TargetServiceImpl.class);
    private final UserService userService;
    private final FinanceService financeService;


    public TargetServiceImpl(UserService userService, FinanceService financeService) {
        this.financeService = financeService;
        this.userService = userService;
    }


    @Override
    public void setMonthlyBudget(double budget) {
        Finance finance = this.findFinance(UserContext.getCurrentUser().email());
        finance.setMonthlyBudget(budget);
        financeService.save(finance);
        log.debug("Месячный бюджет установлен: {}", budget);
    }

    @Override
    public void checkBudgetExceeded(String email) {
        Finance finance = this.findFinance(UserContext.getCurrentUser().email());

        List<TransactionDto> transactions = financeService.getTransactions(email);
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        double totalExpenses = transactions.stream()
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE && t.date().isAfter(thirtyDaysAgo))
                .mapToDouble(TransactionDto::amount)
                .sum();

        double monthlyBudget = finance.getMonthlyBudget();
        if (totalExpenses > monthlyBudget) {
            System.out.println("Внимание! Вы превысили месячный бюджет на " + (totalExpenses - monthlyBudget) + "!");
        }
    }

    @Override
    public String generateFinancialReport() {
        UserDto user = userService.getUserByEmail(UserContext.getCurrentUser().email());
        Finance finance = financeService.findFinanceById(user.financeId());
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("==== Финансовый отчет ====\n");
        reportBuilder.append("Текущие накопления: ").append(finance.getCurrentSavings()).append("\n");
        reportBuilder.append("Цель накопления: ").append(finance.getSavingsGoal()).append("\n");
        reportBuilder.append("Прогресс к цели: ").append(financeService.getProgressTowardsGoal(user.email())).append("%\n");

        reportBuilder.append("Суммарный доход за период: ")
                .append(financeService.getTotalIncome(LocalDate.now().minusMonths(1), LocalDate.now(), user.email())).append("\n");
        reportBuilder.append("Суммарные расходы за период: ")
                .append(financeService.getTotalExpenses(LocalDate.now().minusMonths(1), LocalDate.now(), user.email())).append("\n");

        reportBuilder.append("Расходы по категориям:\n");
        financeService.getExpensesByCategory(UserContext.getCurrentUser().email()).forEach((category, total) ->
                reportBuilder.append(category).append(": ").append(total).append("\n"));

        reportBuilder.append("===========================\n");

        return reportBuilder.toString();
    }

    @Override
    public void updateGoalSavings(double savingGoal) {
        Finance finance = this.findFinance(UserContext.getCurrentUser().email());
        finance.setSavingsGoal(savingGoal);
        financeService.save(finance);
        log.debug("Цель накопления установлена: {}", savingGoal);
    }

    private Finance findFinance(String email) {
        UserDto user = userService.getUserByEmail(email);
        return financeService.findFinanceById(user.financeId());
    }

}
