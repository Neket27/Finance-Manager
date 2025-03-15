package app.repository.bd;

import app.entity.Transaction;
import app.repository.TransactionRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransactionTableInMemory implements TransactionRepository {
    private static Long count = 0L;
    private final Map<Long, Transaction> inMemoryDatabase = new HashMap<>();

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(inMemoryDatabase.get(id - 1));
    }

    @Override
    public Transaction save(Transaction entity) {
        try {
            if (inMemoryDatabase.containsKey(entity.getId() - 1))
                inMemoryDatabase.put(entity.getId() - 1, entity);
            else {
                inMemoryDatabase.put(count++, entity);
                entity.setId(count);
            }
        } catch (NullPointerException e) {
            inMemoryDatabase.put(count++, entity);
            entity.setId(count);
        }


        return entity;
    }

    @Override
    public void delete(Transaction entity) {
        inMemoryDatabase.remove(entity.getId() - 1);
    }

    public void deleteById(Long id) {
        inMemoryDatabase.remove(id - 1);
    }

    @Override
    public Collection<Transaction> getAll() {
        return inMemoryDatabase.values();
    }
}
