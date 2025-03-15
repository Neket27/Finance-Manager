package app.dto.finance;

import java.util.ArrayList;
import java.util.List;

public record CreateFinanceDto(
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
        private Double monthlyBudget;
        private Double savingsGoal;
        private Double currentSavings;
        private Double totalExpenses;
        private List<Long> transactionsId = new ArrayList<>();

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

        public CreateFinanceDto build() {
            return new CreateFinanceDto(monthlyBudget, savingsGoal, currentSavings, totalExpenses, transactionsId);
        }
    }
}

