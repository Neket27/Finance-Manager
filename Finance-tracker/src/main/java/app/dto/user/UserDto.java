package app.dto.user;

import app.entity.Role;
import jakarta.validation.constraints.*;

public record UserDto(

        @NotNull(message = "ID пользователя не может быть null")
        @Positive(message = "ID пользователя должен быть положительным числом")
        Long id,

        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
        String name,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password,

        @NotNull(message = "Активность пользователя должна быть указана")
        Boolean isActive,

        @NotNull(message = "Роль не может быть null")
        Role role,

        @NotNull(message = "ID финансов должен быть указан")
        @Positive(message = "ID финансов должен быть положительным числом")
        Long financeId
) {


    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private Boolean isActive;
        private Role role;
        private Long financeId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder finance(Long financeId) {
            this.financeId = financeId;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, name, email, password, isActive, role, financeId);
        }
    }
}

