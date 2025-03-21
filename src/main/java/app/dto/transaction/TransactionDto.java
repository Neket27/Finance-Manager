package app.dto.transaction;

import app.entity.TypeTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        Long id,
        BigDecimal amount,
        String category,
        Instant date,
        String description,
        TypeTransaction typeTransaction,
        Long financeId
) {
    public static class Builder {
        private Long id;
        private BigDecimal amount = BigDecimal.ZERO;
        private String category;
        private Instant date;
        private String description;
        private TypeTransaction typeTransaction;
        Long financeId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount != null ? amount : BigDecimal.ZERO;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder date(Instant date) {
            this.date = date;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder typeTransaction(TypeTransaction typeTransaction) {
            this.typeTransaction = typeTransaction;
            return this;
        }

        public Builder financeId(Long financeId) {
            this.financeId = financeId;
            return this;
        }

        public TransactionDto build() {
            return new TransactionDto(id, amount, category, date, description, typeTransaction, financeId);
        }
    }
}
