package app.entity;

import java.util.ArrayList;
import java.util.List;

public class Finance {

    private Long id;
    private double monthlyBudget;
    private double savingsGoal;
    private double currentSavings;
    private double totalExpenses;

    public void setTransactionsId(List<Long> transactionsId) {
        this.transactionsId = transactionsId;
    }

    private List<Long> transactionsId;

    public Finance() {
        transactionsId = new ArrayList<>();
    }

    public Finance(Long id, double monthlyBudget, double savingsGoal, double currentSavings, double totalExpenses, List<Long> transactionsId) {
        this.id = id;
        this.monthlyBudget = monthlyBudget;
        this.savingsGoal = savingsGoal;
        this.currentSavings = currentSavings;
        this.totalExpenses = totalExpenses;
        this.transactionsId = transactionsId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getTransactionsId() {
        return transactionsId;
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

    public static class Builder {
        private Long id;
        private double monthlyBudget;
        private double savingsGoal;
        private double currentSavings;
        private double totalExpenses;
        private List<Long> transactionsId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder monthlyBudget(double monthlyBudget) {
            this.monthlyBudget = monthlyBudget;
            return this;
        }

        public Builder savingsGoal(double savingsGoal) {
            this.savingsGoal = savingsGoal;
            return this;
        }

        public Builder currentSavings(double currentSavings) {
            this.currentSavings = currentSavings;
            return this;
        }

        public Builder totalExpenses(double totalExpenses) {
            this.totalExpenses = totalExpenses;
            return this;
        }

        public Builder transactionsId(List<Long> transactionsId) {
            this.transactionsId = transactionsId;
            return this;
        }

        public Finance build() {
            return new Finance(id, monthlyBudget, savingsGoal, currentSavings, totalExpenses, transactionsId);
        }
    }
}

