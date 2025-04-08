package app.dto.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record FinanceDto(
        Long id,
        BigDecimal monthlyBudget,
        BigDecimal savingsGoal,
        BigDecimal currentSavings,
        BigDecimal totalExpenses,
        List<Long> transactionsId
) {
    public static Builder builder() {
        return new Builder();
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

        public FinanceDto build() {
            return new FinanceDto(id, monthlyBudget, savingsGoal, currentSavings, totalExpenses, transactionsId);
        }
    }
}
