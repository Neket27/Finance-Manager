package app.service;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;

import java.util.List;
import java.util.Set;

public interface TransactionService {

    Transaction getTransactionById(Long id);

    Transaction edit(Transaction transaction);

    Transaction create(Long financeId, Transaction transaction);

    void delete(Long id);

    List<Transaction> findAll(FinanceDto finance);

    List<Transaction> getFilteredTransactions(FilterTransactionDto filterTransactionDto);

    Set<Transaction> getTransactionsByFinanceId(Long id);
}
