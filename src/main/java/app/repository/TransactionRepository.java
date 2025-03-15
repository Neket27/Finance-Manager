package app.repository;

import app.entity.Transaction;

public interface TransactionRepository extends GenericRepository<Transaction, Long> {

    void deleteById(Long id);
}
