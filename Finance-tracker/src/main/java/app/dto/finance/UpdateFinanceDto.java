package app.dto.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record UpdateFinanceDto(
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
            this.monthlyBudget = monthlyBudget != null ? monthlyBudget : BigDecimal.ZERO;
            return this;
        }

        public Builder savingsGoal(BigDecimal savingsGoal) {
            this.savingsGoal = savingsGoal != null ? savingsGoal : BigDecimal.ZERO;
            return this;
        }

        public Builder currentSavings(BigDecimal currentSavings) {
            this.currentSavings = currentSavings != null ? currentSavings : BigDecimal.ZERO;
            return this;
        }

        public Builder totalExpenses(BigDecimal totalExpenses) {
            this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
            return this;
        }

        public Builder transactionsId(List<Long> transactionsId) {
            this.transactionsId = transactionsId != null ? transactionsId : new ArrayList<>();
            return this;
        }

        public UpdateFinanceDto build() {
            return new UpdateFinanceDto(id, monthlyBudget, savingsGoal, currentSavings, totalExpenses, transactionsId);
        }
    }
}
