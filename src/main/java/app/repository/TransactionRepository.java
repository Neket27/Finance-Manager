package app.repository;

import app.entity.Transaction;
import app.entity.TypeTransaction;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends GenericRepository<Transaction, Long> {

    void deleteById(Long id);

    List<Transaction> findByFinanceId(Long id);

    List<Transaction> getFilteredTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction);

    void deleteAllByFinanceId(Long financeId);
}
