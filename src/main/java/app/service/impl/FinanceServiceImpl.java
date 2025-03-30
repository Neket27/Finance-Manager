package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;
import app.entity.TypeTransaction;
import app.exception.common.CreateException;
import app.exception.common.DeleteException;
import app.exception.common.NotFoundException;
import app.exception.common.UpdateException;
import app.mapper.FinanceMapper;
import app.repository.FinanceRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@CustomLogging
public class FinanceServiceImpl implements FinanceService {

    private final Logger log = LoggerFactory.getLogger(FinanceServiceImpl.class);
    private final FinanceRepository financeRepository;
    private final TransactionService transactionService;
    private final FinanceMapper financeMapper;

    public FinanceServiceImpl(FinanceRepository financeRepository, TransactionService transactionService, FinanceMapper financeMapper) {
        this.financeRepository = financeRepository;
        this.transactionService = transactionService;
        this.financeMapper = financeMapper;
    }

    @Override
    @Auditable
    public Long createEmptyFinance(CreateFinanceDto dto) {
        Finance finance = financeMapper.toEntity(dto);
        finance = financeRepository.save(finance);
        return finance.getId();
    }

    @Override
    @Auditable
    public TransactionDto createTransaction(Long financeId, CreateTransactionDto dto) {
        Finance finance = find(financeId);

        if (dto.typeTransaction().equals(TypeTransaction.EXPENSE) &&
                finance.getCurrentSavings().compareTo(dto.amount()) < 0) {
            BigDecimal diff = dto.amount().subtract(finance.getCurrentSavings());
            System.out.println("Недостаточно средств, не хватает: " + diff);
            throw new CreateException("Недостаточно средств");
        }

        TransactionDto transaction = transactionService.create(financeId, dto);
        finance.getTransactionsId().add(transaction.id());

        updateCurrentSavings(finance, transaction.amount(), transaction.typeTransaction());
        financeRepository.save(finance);
        return transaction;
    }

    private void updateCurrentSavings(Finance finance, BigDecimal amount, TypeTransaction type) {
        if (type == TypeTransaction.PROFIT) {
            finance.setCurrentSavings(finance.getCurrentSavings().add(amount));
        } else {
            finance.setCurrentSavings(finance.getCurrentSavings().subtract(amount));
        }
    }

    private Finance find(Long id) {
        return financeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Finance not found id=" + id));
    }

    @Override
    @Auditable
    public Map<String, BigDecimal> getExpensesByCategory(Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(TransactionDto::category,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDto::amount, BigDecimal::add)));
    }

    @Override
    @Auditable
    public void delete(Long financeId, Long idTransaction) {
        Set<TransactionDto> transactionsByFinanceId = transactionService.getTransactionsByFinanceId(financeId);
        boolean transactionExists = transactionsByFinanceId.stream()
                .anyMatch(transaction -> transaction.id().equals(idTransaction));

        if (transactionExists) {
            transactionService.delete(idTransaction);
            Finance finance = this.find(financeId);
            finance.getTransactionsId().remove(idTransaction);
            financeRepository.save(finance);
        } else {
            throw new DeleteException("Transaction in finance: " + financeId + " not found, id transaction: " + idTransaction);
        }
    }

    @Override
    @Auditable
    public void updatetMonthlyBudget(Long financeId, BigDecimal budget) {
        Finance finance = find(financeId);
        finance.setMonthlyBudget(budget);
        save(finance);
    }

    @Override
    @Auditable
    public List<TransactionDto> filterTransactions(Long financeId, FilterTransactionDto filterTransactionDto) {
        return transactionService.getFilteredTransactions(filterTransactionDto);
    }

    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    @Override
    @Auditable
    public BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return getTotal(finance, startDate, endDate, TypeTransaction.PROFIT);
    }

    @Override
    @Auditable
    public BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate, Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return getTotal(finance, startDate, endDate, TypeTransaction.EXPENSE);
    }

    private BigDecimal getTotal(FinanceDto finance, LocalDate startDate, LocalDate endDate, TypeTransaction typeTransaction) {
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction().equals(typeTransaction))
                .filter(t -> isWithinDateRange(t.date(), startDate, endDate))
                .map(TransactionDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Auditable
    public TransactionDto editTransaction(Long financeId, UpdateTransactionDto updateTransactionDto) {
        Set<TransactionDto> transactionsByFinanceId = transactionService.getTransactionsByFinanceId(financeId);
        boolean transactionExists = transactionsByFinanceId.stream()
                .anyMatch(transaction -> transaction.id().equals(updateTransactionDto.id()));

        if (transactionExists) {
            TransactionDto transaction = transactionService.edit(updateTransactionDto);
            log.debug("Edited transaction: {}", updateTransactionDto);
            return transaction;
        } else {
            log.error("Failed to edit transaction: {}", updateTransactionDto);
            throw new UpdateException("Failed to edit transaction: " + updateTransactionDto);
        }
    }

    @Override
    @Auditable
    public Finance save(Finance finance) {
        return financeRepository.save(finance);
    }

    @Override
    @Auditable
    public Set<TransactionDto> list(Long financeId) {
        return transactionService.getTransactionsByFinanceId(financeId);
    }

    @Override
    @Auditable
    public FinanceDto getFinanceById(Long id) {
        return financeMapper.toDto(this.find(id));
    }

    @Override
    @Auditable
    public Finance findFinanceById(Long id) {
        return this.find(id);
    }

    private FinanceDto getFinance(Long financeId) {
        Finance finance = find(financeId);
        Set<TransactionDto> transactions = transactionService.getTransactionsByFinanceId(finance.getId());
        finance.setTransactionsId(transactions.stream().map(TransactionDto::id).collect(Collectors.toList()));
        return financeMapper.toDto(finance);
    }
}
