package app.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {

    private Long id;
    private BigDecimal amount;
    private String category;
    private Instant date;
    private String description;
    private TypeTransaction typeTransaction;
    private Long financeId;

    public Transaction(Long id, BigDecimal amount, String category, Instant date, String description, TypeTransaction typeTransaction, Long financeId) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.typeTransaction = typeTransaction;
        this.financeId = financeId;
    }

    public Transaction() {
        this.amount = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFinanceId() {
        return financeId;
    }

    public void setFinanceId(Long financeId) {
        this.financeId = financeId;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public Instant getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public TypeTransaction getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(TypeTransaction typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class Builder {
        private Long id;
        private BigDecimal amount = BigDecimal.ZERO;
        private String category;
        private Instant date;
        private String description;
        private TypeTransaction typeTransaction;
        private Long financeId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder financeId(Long financeId) {
            this.financeId = financeId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
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

        public Transaction build() {
            return new Transaction(id, amount, category, date, description, typeTransaction, financeId);
        }
    }
}
