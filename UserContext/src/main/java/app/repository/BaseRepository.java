package app.repository;

import java.util.Collection;
import java.util.Optional;

public interface BaseRepository<T, ID> {

    Optional<T> findById(ID id);

    T save(T entity);

    void delete(T entity);

    Collection<T> getAll();

}

