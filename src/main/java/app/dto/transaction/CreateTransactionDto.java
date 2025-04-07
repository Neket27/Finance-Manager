package app.dto.transaction;

import app.entity.TypeTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateTransactionDto(

        @NotNull(message = "Сумма не может быть null")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @NotBlank(message = "Категория не должна быть пустой")
        @Size(max = 100, message = "Категория должна содержать не более 100 символов")
        String category,

        @Size(max = 255, message = "Описание должно содержать не более 255 символов")
        String description,

        @NotNull(message = "Тип транзакции должен быть указан")
        TypeTransaction typeTransaction
) {
}
