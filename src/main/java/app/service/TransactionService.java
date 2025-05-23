package app.service;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;
import app.entity.TypeTransaction;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface TransactionService {

    TransactionDto getTransactionById(Long id);

    TransactionDto edit(UpdateTransactionDto updateTransactionDto);

    TransactionDto create(Long financeId, CreateTransactionDto dto);

    void delete(Long id);

    List<TransactionDto> findAll(FinanceDto finance);

    List<TransactionDto> getFilteredTransactions(FilterTransactionDto filterTransactionDto);

    Set<TransactionDto> getTransactionsByFinanceId(Long id);
}
