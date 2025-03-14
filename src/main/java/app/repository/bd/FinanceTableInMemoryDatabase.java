package app.repository.bd;

import app.entity.Finance;
import app.repository.FinanceRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FinanceTableInMemoryDatabase implements FinanceRepository {

    private static Long count = 0L;
    private final Map<Long, Finance> inMemoryDatabase = new HashMap<>();

    @Override
    public Optional<Finance> findById(Long id) {
        return Optional.ofNullable(inMemoryDatabase.get(id - 1));
    }

    @Override
    public Finance save(Finance entity) {
        try {

            if (inMemoryDatabase.containsKey(entity.getId() - 1))
                inMemoryDatabase.put(entity.getId() - 1, entity);
            else {
                inMemoryDatabase.put(count++, entity);
                entity.setId(count);
            }
        }catch (NullPointerException e){
            inMemoryDatabase.put(count++, entity);
            entity.setId(count);
        }

        return entity;
    }

    @Override
    public void delete(Finance entity) {
        inMemoryDatabase.remove(entity.getId() - 1);
    }

    @Override
    public Collection<Finance> getAll() {
        return inMemoryDatabase.values();
    }
}
