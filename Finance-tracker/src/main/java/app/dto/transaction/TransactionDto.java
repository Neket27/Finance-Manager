package app.dto.transaction;

import app.entity.TypeTransaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TransactionDto(

        @NotNull(message = "ID не может быть пустым")
        @Positive(message = "ID должен быть положительным числом")
        Long id,

        @NotNull(message = "Сумма не может быть пустой")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @NotBlank(message = "Категория не может быть пустой")
        @Size(max = 100, message = "Категория должна содержать не более 100 символов")
        String category,

        @NotNull(message = "Дата не может быть пустой")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant date,

        @Size(max = 255, message = "Описание должно содержать не более 255 символов")
        String description,

        @NotNull(message = "Тип транзакции не может быть пустым")
        TypeTransaction typeTransaction,

        @NotNull(message = "Finance ID не может быть пустым")
        @Positive(message = "Finance ID должен быть положительным числом")
        Long financeId
) {
}
