package app.service.impl;

import app.dto.finance.FinanceDto;
import app.dto.transaction.FilterTransactionDto;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.common.CreateException;
import app.exception.common.DeleteException;
import app.exception.common.NotFoundException;
import app.exception.common.UpdateException;
import app.mapper.FinanceMapper;
import app.repository.FinanceRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@CustomLogging
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final FinanceRepository financeRepository;
    private final TransactionService transactionService;
    private final FinanceMapper financeMapper;

    @Override
    @Auditable
    @Transactional
    public Long createEmptyFinance(Finance finance) {
        finance = financeRepository.save(finance);
        return finance.getId();
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Transaction createTransaction(Long financeId, Transaction transaction) {
        Finance finance = find(financeId);

        if (transaction.getTypeTransaction().equals(TypeTransaction.EXPENSE) &&
                finance.getCurrentSavings().compareTo(transaction.getAmount()) < 0) {
            BigDecimal diff = transaction.getAmount().subtract(finance.getCurrentSavings());
            System.out.println("Недостаточно средств, не хватает: " + diff);
            throw new CreateException("Недостаточно средств");
        }

        Transaction returnedTransaction = transactionService.create(financeId, transaction);
        finance.getTransactionsId().add(transaction.getId());

        updateCurrentSavings(finance, returnedTransaction.getAmount(), returnedTransaction.getTypeTransaction());
        financeRepository.save(finance);
        return returnedTransaction;
    }

    private void updateCurrentSavings(app.entity.Finance finance, BigDecimal amount, TypeTransaction type) {
        if (type == TypeTransaction.PROFIT) {
            finance.setCurrentSavings(finance.getCurrentSavings().add(amount));
        } else {
            finance.setCurrentSavings(finance.getCurrentSavings().subtract(amount));
        }
    }

    private app.entity.Finance find(Long id) {
        return financeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Finance not found id=" + id));
    }

    @Override
    @Auditable
    @Transactional
    public Map<String, BigDecimal> getExpensesByCategory(Long financeId) {
        FinanceDto finance = getFinance(financeId);
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.getTypeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long financeId, Long idTransaction) {
        Set<Transaction> transactionsByFinanceId = transactionService.getTransactionsByFinanceId(financeId);
        boolean transactionExists = transactionsByFinanceId.stream()
                .anyMatch(transaction -> transaction.getId().equals(idTransaction));

        if (transactionExists) {
            transactionService.delete(idTransaction);
            app.entity.Finance finance = this.find(financeId);
            finance.getTransactionsId().remove(idTransaction);
            financeRepository.save(finance);
        } else {
            throw new DeleteException("Transaction in finance: " + financeId + " not found, id transaction: " + idTransaction);
        }
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void updatetMonthlyBudget(Long financeId, BigDecimal budget) {
        app.entity.Finance finance = find(financeId);
        finance.setMonthlyBudget(budget);
        save(finance);
    }

    @Override
    @Auditable
    @Transactional
    public List<Transaction> filterTransactions(Long financeId, FilterTransactionDto filterTransactionDto) {
        return transactionService.getFilteredTransactions(filterTransactionDto);
    }

    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    @Override
    @Auditable
    @Transactional
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
                .filter(t -> t.getTypeTransaction().equals(typeTransaction))
                .filter(t -> isWithinDateRange(t.getDate(), startDate, endDate))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Transaction editTransaction(Long financeId, Transaction updateTransaction) {
        Set<Transaction> transactionsByFinanceId = transactionService.getTransactionsByFinanceId(financeId);
        boolean transactionExists = transactionsByFinanceId.stream()
                .anyMatch(transaction -> transaction.getId().equals(updateTransaction.getId()));

        if (transactionExists) {
            Transaction transaction = transactionService.edit(updateTransaction);
            log.debug("Edited transaction: {}", updateTransaction);
            return transaction;
        } else {
            log.error("Failed to edit transaction: {}", updateTransaction);
            throw new UpdateException("Failed to edit transaction: " + updateTransaction);
        }
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Finance save(app.entity.Finance finance) {
        return financeRepository.save(finance);
    }

    @Override
    @Auditable
    @Transactional
    public Set<Transaction> list(Long financeId) {
        return transactionService.getTransactionsByFinanceId(financeId);
    }

    @Override
    @Auditable
    @Transactional
    public Finance getFinanceById(Long id) {
        return find(id);
    }

    @Override
    @Auditable
    @Transactional
    public Finance findFinanceById(Long id) {
        return this.find(id);
    }

    private FinanceDto getFinance(Long financeId) {
        Finance finance = find(financeId);
        Set<Transaction> transactions = transactionService.getTransactionsByFinanceId(finance.getId());
        finance.setTransactionsId(transactions.stream().map(Transaction::getId).collect(Collectors.toList()));
        return financeMapper.toDto(finance);
    }
}
