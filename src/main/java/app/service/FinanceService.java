package app.service;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Finance;
import app.entity.TypeTransaction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceService {

    TransactionDto addTransactionUser(CreateTransactionDto dto);

    double getProgressTowardsGoal(String email);

    List<TransactionDto> filterTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction, String email);

    Map<String, Double> getExpensesByCategory(String email);

    double getTotalIncome(LocalDate startDate, LocalDate endDate, String email);

    double getTotalExpenses(LocalDate startDate, LocalDate endDate, String email);

    boolean removeTransactionUser(Long id);

    TransactionDto editTransaction(UpdateTransactionDto updateTransactionDto);

    Finance save(Finance finance);

    List<TransactionDto> getTransactions(String userId);

    Boolean checkExpenseLimit(String email);

    FinanceDto getFinanceById(Long id);

    Finance findFinanceById(Long id);
}
