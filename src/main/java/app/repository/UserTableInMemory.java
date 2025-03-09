package app.repository;

import app.entity.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserTableInMemory implements UserRepository {
    private final Map<String, User> inMemoryDatabase = new HashMap<>();

    @Override
    public Optional<User> findByEmail(String key) {
        return Optional.ofNullable(inMemoryDatabase.get(key));
    }

    @Override
    public User save(User entity) {
        inMemoryDatabase.put(entity.getEmail(), entity); // Предполагаем, что у User есть метод getEmail()
        return entity;
    }

    @Override
    public void delete(User entity) {
        inMemoryDatabase.remove(entity.getEmail());
    }

    @Override
    public boolean existsByEmail(String email) {
        return inMemoryDatabase.containsKey(email);
    }

    @Override
    public Collection<User> getAll() {
        return inMemoryDatabase.values();
    }


}
