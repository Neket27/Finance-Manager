package app.dto.user;

import app.entity.Role;

public record UpdateUserDto(
        String name,
        String email,
        String password,
        Role role,
        Long financeId
) {

    public UpdateUserDto(String name, String email, String password, Role role, Long financeId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.financeId = financeId;
    }

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
