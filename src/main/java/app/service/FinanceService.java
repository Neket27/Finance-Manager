package app.service;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;
import app.entity.TypeTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceService {

    TransactionDto addTransactionUser(CreateTransactionDto dto);

    Boolean transactionAmountExceedsBalance(String email, BigDecimal transactionAmount);

    Double getProgressTowardsGoal(String email);

    List<TransactionDto> filterTransactions(FilterTransactionDto filterTransactionDto);

    Map<String, BigDecimal> getExpensesByCategory(String email);

    BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, String email);

    BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate, String email);

    boolean removeTransactionUser(Long id);

    TransactionDto editTransaction(UpdateTransactionDto updateTransactionDto);

    Finance save(Finance finance);

    List<TransactionDto> getTransactions(String userId);

    Boolean checkMonthlyExpenseLimit(String email);

    FinanceDto getFinanceById(Long id);

    Finance findFinanceById(Long id);
}
