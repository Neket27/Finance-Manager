package app.dto.user;

import app.entity.Finance;
import app.entity.Role;

public record UserDto(
        String name,
        String email,
        String password,
        Boolean isActive,
        Role role,
        Finance finance
) {

    public UserDto(String name, String email, String password, Boolean isActive,Role role,Finance finance) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.role = role;
        this.finance = finance;
    }

    public UserDto(String email, String password) {
        this(null, email, password, null,null, null);
    }

    public static class Builder {
        private String name;
        private String email;
        private String password;
        private Boolean isActive;
        private Role role;
        private Finance finance;

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

        public Builder finance(Finance finance) {
            this.finance = finance;
            return this;
        }

        public UserDto build() {
            return new UserDto(name, email, password,isActive,role, finance);
        }
    }
}

