package app.dto.user;

import app.entity.Finance;
import app.entity.Role;

public record UpdateUserDto(
        String name,
        String email,
        String password,
        Role role,
        Finance finance
) {

    public UpdateUserDto(String email, String password) {
        this(null, email, password, null, null);
    }

    public UpdateUserDto(String name, String email, String password, Role role, Finance finance) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.finance = finance;
    }

    public static class Builder {
        private String name;
        private String email;
        private String password;
        private Role role;
        private Finance finance;

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

        public UpdateUserDto build() {
            return new UpdateUserDto(name, email, password, role, finance);
        }
    }

}
