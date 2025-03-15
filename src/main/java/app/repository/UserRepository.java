package app.repository;

import app.entity.User;

public interface UserRepository extends GenericRepository<User, String> {

    boolean existsByEmail(String email);

}
