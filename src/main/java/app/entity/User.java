package app.entity;

public class User {
    private String email;
    private String name;
    private String password;
    private boolean isActive;
    private Role role;
    private Long financeId;

    public Long getFinanceId() {
        return financeId;
    }

    public void setFinanceId(Long financeId) {
        this.financeId = financeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public User(String name, String email, String password, Role role, Long financeId, boolean isActive) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.financeId = financeId;
        this.isActive = isActive;
    }

    public User(String name, String email, String password, Role role, Long financeId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.financeId = financeId;
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
        private Long financeId;
        private boolean isActive;

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder finance(Long financeId) {
            this.financeId = financeId;
            return this;
        }


        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder setName(String name) {
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

        public User build() {
            return new User(name, email, password, role,financeId);
        }
    }
}
