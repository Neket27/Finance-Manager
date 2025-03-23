package app.dto.transaction;

import app.entity.TypeTransaction;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateTransactionDto(

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
        Instant date,

        @Size(max = 255, message = "Описание должно содержать не более 255 символов")
        String description,

        @NotNull(message = "Тип транзакции не может быть пустым")
        TypeTransaction typeTransaction
) {

    public static class Builder {
        private Long id;
        private BigDecimal amount = BigDecimal.ZERO;
        private String category;
        private Instant date;
        private String description;
        private TypeTransaction typeTransaction;

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

        public UpdateTransactionDto build() {
            return new UpdateTransactionDto(id, amount, category, date, description, typeTransaction);
        }
    }
}
