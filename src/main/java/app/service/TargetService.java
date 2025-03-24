package app.service;

import java.math.BigDecimal;

public interface TargetService {

    void setMonthlyBudget(BigDecimal budget);

    Boolean isMonthBudgetExceeded(Long financeId);

    String generateFinancialReport();

    void updateGoalSavings(BigDecimal savingGoal);
}
