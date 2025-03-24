package app.service;

import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceService {


    Long createEmptyFinance(CreateFinanceDto dto);

    TransactionDto addTransaction(Long financeId, CreateTransactionDto dto);

    BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, Long financeId);

    BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate, Long financeId);

    Double getProgressTowardsGoal(Long financeId);

    List<TransactionDto> filterTransactions(FilterTransactionDto filterTransactionDto);

    boolean removeTransactionUser(Long idTransaction, Long financeId);

    TransactionDto editTransaction(UpdateTransactionDto updateTransactionDto);

    Finance save(Finance finance);

    List<TransactionDto> getTransactions(Long financeId);

    FinanceDto getFinanceById(Long id);

    Finance findFinanceById(Long id);

    Map<String, BigDecimal> getExpensesByCategory(Long financeId);
}
