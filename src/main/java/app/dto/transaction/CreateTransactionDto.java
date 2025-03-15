package app.dto.transaction;

import app.entity.TypeTransaction;

import java.time.Instant;

public record CreateTransactionDto(
        Double amount,
        String category,
        Instant date,
        String description,
        TypeTransaction typeTransaction
) {
}
