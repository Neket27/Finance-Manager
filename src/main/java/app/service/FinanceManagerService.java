package app.service;

import app.entity.Transaction;
import app.entity.TypeTransaction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceManagerService {
    void setMonthlyBudget(double budget);

    void addExpense(double amount);

    void setSavingsGoal(double goal);

    void addTransaction(double amount, String category, Instant date, String description, TypeTransaction typeTransaction, String email);

    void editTransaction(int index, double amount, String category, String description);

    void deleteTransaction(int index);

    void checkBudgetExceeded(String email);

    void updateSavings(double amount, TypeTransaction type);

    double getProgressTowardsGoal(String email);

    List<Transaction> filterTransactions(Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction, String email);

    //     Анализ расходов по категориям
    Map<String, Double> getExpensesByCategory(String email);

    // Расчёт суммарного дохода и расхода за определённый период
    double getTotalIncome(LocalDate startDate, LocalDate endDate, String email);

    double getTotalExpenses(LocalDate startDate, LocalDate endDate, String email);

    void checkExpenseLimit(String email);

    // Формирование отчёта по финансовому состоянию
    void generateFinancialReport(String email);
}
