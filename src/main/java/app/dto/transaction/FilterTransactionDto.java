package app.dto.transaction;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record FilterTransactionDto(

        Instant startDate,
        Instant endDate,

        @Size(max = 100, message = "Категория должна содержать не более 100 символов")
        String category,

        @Pattern(
                regexp = "^(INCOME|EXPENSE)?$",
                message = "Тип транзакции должен быть 'INCOME', 'EXPENSE' или пустым"
        )
        String typeTransaction
) {
}