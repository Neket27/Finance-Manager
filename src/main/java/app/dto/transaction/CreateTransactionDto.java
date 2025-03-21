package app.dto.transaction;

import app.entity.TypeTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionDto(
        BigDecimal amount,
        String category,
        Instant date,
        String description,
        TypeTransaction typeTransaction
) {

    public static class Builder {
        private BigDecimal amount = BigDecimal.ZERO;
        private String category;
        private Instant date;
        private String description;
        private TypeTransaction typeTransaction;

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

        public CreateTransactionDto build() {
            return new CreateTransactionDto(amount, category, date, description, typeTransaction);
        }
    }
}
