package app.dto.user;

import app.entity.Role;

public record UserDto(
        Long id,
        String name,
        String email,
        String password,
        Boolean isActive,
        Role role,
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

