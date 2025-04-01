package app.entity;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//@SequenceGenerator(name = "finance_seq", sequenceName = "finance_id_seq", allocationSize = 1)
public class Finance {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "finance_seq")
    private Long id;
    private BigDecimal monthlyBudget;
    private BigDecimal savingsGoal;
    private BigDecimal currentSavings;
    private BigDecimal totalExpenses;
    private List<Long> transactionsId;

    public Finance() {
        transactionsId = new ArrayList<>();
        monthlyBudget = BigDecimal.ZERO;
        savingsGoal = BigDecimal.ZERO;
        currentSavings = BigDecimal.ZERO;
        totalExpenses = BigDecimal.ZERO;
    }

    public Finance(Long id, BigDecimal monthlyBudget, BigDecimal savingsGoal, BigDecimal currentSavings, BigDecimal totalExpenses, List<Long> transactionsId) {
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

    public void setTransactionsId(List<Long> transactionsId) {
        this.transactionsId = transactionsId;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public BigDecimal getSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(BigDecimal savingsGoal) {
        this.savingsGoal = savingsGoal;
    }

    public BigDecimal getCurrentSavings() {
        return currentSavings;
    }

    public void setCurrentSavings(BigDecimal currentSavings) {
        this.currentSavings = currentSavings;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public static class Builder {
        private Long id;
        private BigDecimal monthlyBudget = BigDecimal.ZERO;
        private BigDecimal savingsGoal = BigDecimal.ZERO;
        private BigDecimal currentSavings = BigDecimal.ZERO;
        private BigDecimal totalExpenses = BigDecimal.ZERO;
        private List<Long> transactionsId = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder monthlyBudget(BigDecimal monthlyBudget) {
            this.monthlyBudget = monthlyBudget;
            return this;
        }

        public Builder savingsGoal(BigDecimal savingsGoal) {
            this.savingsGoal = savingsGoal;
            return this;
        }

        public Builder currentSavings(BigDecimal currentSavings) {
            this.currentSavings = currentSavings;
            return this;
        }

        public Builder totalExpenses(BigDecimal totalExpenses) {
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
