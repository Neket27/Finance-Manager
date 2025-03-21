package app.service;

import java.math.BigDecimal;

public interface TargetService {

    void setMonthlyBudget(BigDecimal budget);

    Boolean isMonthBudgetExceeded(String email);

    String generateFinancialReport();

    void updateGoalSavings(BigDecimal savingGoal);
}
