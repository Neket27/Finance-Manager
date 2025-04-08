package app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
        String name,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password
) {
}
