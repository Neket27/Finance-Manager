package app.service.impl;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exeption.NotFoundException;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionServiceImpl implements TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionDto create(CreateTransactionDto dto) {
        try {
            Transaction transaction = transactionMapper.toEntity(dto);
            transaction = transactionRepository.save(transaction);
            log.debug("addTransaction: {}", transaction.toString());
            return transactionMapper.toDto(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Error adding transaction", e);
        }
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        return transactionMapper.toDto(find(id));
    }

    private Transaction find(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
    }

    @Override
    public Transaction edit(UpdateTransactionDto dto) {
        Transaction transaction = this.find(dto.id());
        transactionMapper.updateEntity(transaction, dto);
        transactionRepository.save(transaction);
        log.debug("Транзакция обновлена: {}", transaction);
        return transaction;
    }

    @Override
    public boolean delete(Long id) {
        try {
            transactionRepository.deleteById(id);
            log.debug("deleteTransaction with id: {}", id);
            return true;

        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<TransactionDto> findAll(FinanceDto finance) {
        return finance.transactionsId().stream().map(this::getTransactionById).toList();
    }

    @Override
    public List<TransactionDto> getFilteredTransactions(List<Long> transactionsId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction) {
        return transactionsId.stream()
                .map(this::find)
                .filter(t -> isTransactionValid(t, startDate, endDate, category, typeTransaction))
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    private boolean isTransactionValid(Transaction t, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction) {
        boolean isAfterStartDate = startDate == null || t.getDate().isAfter(startDate);
        boolean isBeforeEndDate = endDate == null || t.getDate().isBefore(endDate);
        boolean isCategoryMatch = category.isEmpty() || t.getCategory().equalsIgnoreCase(category);
        boolean isTypeMatch = typeTransaction == null || t.getTypeTransaction() == typeTransaction;

        return isAfterStartDate && isBeforeEndDate && isCategoryMatch && isTypeMatch;
    }

}


