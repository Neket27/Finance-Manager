package app.dto.user;

import app.entity.Role;

public record UserDto(
        String name,
        String email,
        String password,
        Boolean isActive,
        Role role,
        Long financeId
) {

    public UserDto(String name, String email, String password, Boolean isActive, Role role, Long financeId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.role = role;
        this.financeId = financeId;
    }


    public static class Builder {
        private String name;
        private String email;
        private String password;
        private Boolean isActive;
        private Role role;
        private Long financeId;

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
            return new UserDto(name, email, password, isActive, role, financeId);
        }
    }
}

