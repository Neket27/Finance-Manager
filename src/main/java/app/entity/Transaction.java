package app.entity;

import java.time.Instant;

public class Transaction {
    private double amount;
    private String category;
    private Instant date;
    private String description;
    private TypeTransaction typeTransaction;

    public Transaction(double amount, String category, Instant date, String description, TypeTransaction isIncome) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.typeTransaction = isIncome;
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

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
