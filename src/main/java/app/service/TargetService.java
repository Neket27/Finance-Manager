package app.service;

public interface TargetService {

    void setMonthlyBudget(double budget);

    void checkBudgetExceeded(String email);

    String generateFinancialReport();

    void updateGoalSavings(double savingGoal);
}
