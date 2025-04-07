package app.service;

import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;
import app.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FinanceService {

    Long createEmptyFinance(Finance finance);

    Transaction createTransaction(Long financeId, Transaction transaction);

    BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, Long financeId);

    BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate, Long financeId);

    List<Transaction> filterTransactions(Long financeId, FilterTransactionDto filterTransactionDto);

    Transaction editTransaction(Long financeId, Transaction updateTransaction);

    Finance save(Finance finance);

    Set<Transaction> list(Long financeId);

    Finance getFinanceById(Long id);

    Finance findFinanceById(Long id);

    Map<String, BigDecimal> getExpensesByCategory(Long financeId);

    void delete(Long financeId, Long id);

    void updatetMonthlyBudget(Long financeId, BigDecimal budget);
}
