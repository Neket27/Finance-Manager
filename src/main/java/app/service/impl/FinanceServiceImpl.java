package app.service.impl;

import app.container.Component;
import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;
import app.entity.TypeTransaction;
import app.exception.EditException;
import app.exception.LimitAmountBalance;
import app.exception.NotFoundException;
import app.mapper.FinanceMapper;
import app.repository.FinanceRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
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
    public Long createEmptyFinance(CreateFinanceDto dto) {
        Finance finance = financeMapper.toEntity(dto);
        finance = financeRepository.save(finance);
        return finance.getId();
    }

    @Override
    public TransactionDto addTransaction(Long financeId, CreateTransactionDto dto) {
        Finance finance = find(financeId);

        if (dto.typeTransaction().equals(TypeTransaction.EXPENSE) &&
                finance.getCurrentSavings().compareTo(dto.amount()) < 0) {
            BigDecimal diff = dto.amount().subtract(finance.getCurrentSavings());
            System.out.println("Недостаточно средств, не хватает: " + diff);
            throw new LimitAmountBalance("Недостаточно средств");
        }

        TransactionDto transaction = transactionService.create(dto);
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
    public Double getProgressTowardsGoal(Long financeId) {
        FinanceDto finance = getFinance(financeId);
        BigDecimal current = finance.currentSavings();
        BigDecimal goal = finance.savingsGoal();
        if (goal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return current.divide(goal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategory(Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(TransactionDto::category,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDto::amount, BigDecimal::add)));
    }


    @Override
    public List<TransactionDto> filterTransactions(FilterTransactionDto filterTransactionDto) {
        return transactionService.getFilteredTransactions(filterTransactionDto);
    }

    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    @Override
    public BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return getTotal(finance, startDate, endDate, TypeTransaction.PROFIT);
    }

    @Override
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
    public boolean removeTransactionUser(Long idTransaction, Long financeId) {
        try {
            transactionService.delete(idTransaction);
            Finance finance = this.find(financeId);
            finance.getTransactionsId().remove(idTransaction);
            financeRepository.save(finance);
            log.debug("Removed transaction with idTransaction: {}", idTransaction);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TransactionDto editTransaction(UpdateTransactionDto updateTransactionDto) {
        try {
            TransactionDto transaction = transactionService.edit(updateTransactionDto);
            log.debug("Edited transaction: {}", updateTransactionDto);
            return transaction;
        } catch (Exception e) {
            log.error("Failed to edit transaction: {}", updateTransactionDto);
            throw new EditException("Failed to edit transaction: " + updateTransactionDto);
        }
    }

    @Override
    public Finance save(Finance finance) {
        return financeRepository.save(finance);
    }

    @Override
    public List<TransactionDto> getTransactions(Long financeId) {
        return transactionService.getTransactionsByFinanceId(financeId);
    }

    @Override
    public FinanceDto getFinanceById(Long id) {
        return financeMapper.toDto(this.find(id));
    }

    @Override
    public Finance findFinanceById(Long id) {
        return this.find(id);
    }

    private FinanceDto getFinance(Long financeId) {
        Finance finance = find(financeId);
        List<TransactionDto> transactions = transactionService.getTransactionsByFinanceId(finance.getId());
        finance.setTransactionsId(transactions.stream().map(TransactionDto::id).collect(Collectors.toList()));
        return financeMapper.toDto(finance);
    }
}
