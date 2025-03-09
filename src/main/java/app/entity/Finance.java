package app.entity;

import java.util.List;

public class Finance {

    private final List<Transaction> transactions;
    private double monthlyBudget;
    private double savingsGoal; // Цель накопления
    private double currentSavings; // Текущие накопления
    private double totalExpenses;

    public Finance(List<Transaction> transactions) {
        this.transactions = transactions;
        this.monthlyBudget = 0.0;
        this.savingsGoal = 0.0;
        this.currentSavings = 0.0;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public double getSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(double savingsGoal) {
        this.savingsGoal = savingsGoal;
    }

    public double getCurrentSavings() {
        return currentSavings;
    }

    public void setCurrentSavings(double currentSavings) {
        this.currentSavings = currentSavings;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
}

