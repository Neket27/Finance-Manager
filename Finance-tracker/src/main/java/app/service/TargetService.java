package app.service;

import java.math.BigDecimal;

public interface TargetService {

    void updateMonthlyBudget(Long financeId, BigDecimal budget);

    Boolean isMonthBudgetExceeded(Long financeId);

    Double getProgressTowardsGoal(Long financeId);

    String generateFinancialReport();

    void updateGoalSavings(BigDecimal savingGoal);
}
