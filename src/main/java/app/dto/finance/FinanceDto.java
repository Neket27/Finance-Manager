package app.dto.finance;

import java.util.ArrayList;
import java.util.List;

public record FinanceDto(
        Long id,
        Double monthlyBudget,
        Double savingsGoal,
        Double currentSavings,
        Double totalExpenses,
        List<Long> transactionsId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Double monthlyBudget;
        private Double savingsGoal;
        private Double currentSavings;
        private Double totalExpenses;
        private List<Long> transactionsId = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder monthlyBudget(Double monthlyBudget) {
            this.monthlyBudget = monthlyBudget;
            return this;
        }

        public Builder savingsGoal(Double savingsGoal) {
            this.savingsGoal = savingsGoal;
            return this;
        }

        public Builder currentSavings(Double currentSavings) {
            this.currentSavings = currentSavings;
            return this;
        }

        public Builder totalExpenses(Double totalExpenses) {
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

