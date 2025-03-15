package app.dto.transaction;

import app.entity.TypeTransaction;

import java.time.Instant;

public record TransactionDto(
        Long id,
        Double amount,
        String category,
        Instant date,
        String description,
        TypeTransaction typeTransaction
) {
    public static class Builder {
        private Long id;
        private double amount;
        private String category;
        private Instant date;
        private String description;
        private TypeTransaction typeTransaction;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
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

        public TransactionDto build() {
            return new TransactionDto(id, amount, category, date, description, typeTransaction);
        }
    }
}
