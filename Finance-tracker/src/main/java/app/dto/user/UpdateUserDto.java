package app.dto.user;

import app.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(

        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
        String name,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password,

        @NotNull(message = "Роль не может быть null")
        Role role,

        @NotNull(message = "ID финансов должен быть указан")
        Long financeId
) {


    public static class Builder {
        private String name;
        private String email;
        private String password;
        private Role role;
        private Long financeId;

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

        public UpdateUserDto build() {
            return new UpdateUserDto(name, email, password, role, financeId);
        }
    }

}
