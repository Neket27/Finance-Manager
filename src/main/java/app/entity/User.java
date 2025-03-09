package app.entity;

public class User {
    private String name;
    private String email;
    private String password;
    private boolean isActive;
    private Role role;
    private Finance finance;

    public Finance getFinance() {
        return finance;
    }

    public void setFinance(Finance finance) {
        this.finance = finance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public User(String name, String email, String password, Role role, Finance finance, boolean isActive) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.finance = finance;
        this.isActive = isActive;
    }

    public User(String name, String email, String password, Role role, Finance finance) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.finance = finance;
        this.isActive = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public static class Builder {
        private String name;
        private String email;
        private String password;
        private Role role;
        private Finance finance;
        private boolean isActive;

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder setFinance(Finance finance) {
            this.finance = finance;
            return this;
        }


        public Builder setRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(name, email, password, role,finance);
        }
    }
}
