package app.entity;

import java.time.Instant;

public class Transaction {

    private Long id;
    private double amount;
    private String category;
    private Instant date;
    private String description;
    private TypeTransaction typeTransaction;

    public Transaction(Long id, double amount, String category, Instant date, String description, TypeTransaction isIncome) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.typeTransaction = isIncome;
    }
    public Transaction() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public double getAmount() {
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

    public void setAmount(double amount) {
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

        public Transaction build() {
            return new Transaction(id, amount, category, date, description, typeTransaction);
        }
    }

}
