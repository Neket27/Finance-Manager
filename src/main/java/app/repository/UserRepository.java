package app.repository;

import app.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean tableIsEmpty();

    Optional<User> findByEmail(String email);
}
