package app.repository;

import app.entity.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String key);

    User save(User entity);

    void delete(User entity);

    boolean existsByEmail(String email);

    Collection<User> getAll();
}

