package app.service.impl;

import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.EditException;
import app.exception.LimitAmountBalance;
import app.exception.NotFoundException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.repository.FinanceRepository;
import app.service.FinanceService;
import app.service.NotificationService;
import app.service.TransactionService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceServiceImpl implements FinanceService {

    private final Logger log = LoggerFactory.getLogger(FinanceServiceImpl.class);
    private final FinanceRepository financeRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final FinanceMapper financeMapper;
    private final TransactionMapper transactionMapper;
    private final NotificationService notificationService;

    public FinanceServiceImpl(FinanceRepository financeRepository, UserService userService, TransactionService transactionService, FinanceMapper financeMapper, TransactionMapper transactionMapper, NotificationService notificationService) {
        this.financeRepository = financeRepository;
        this.userService = userService;
        this.transactionService = transactionService;
        this.financeMapper = financeMapper;
        this.transactionMapper = transactionMapper;
        this.notificationService = notificationService;
    }

    @Override
    public TransactionDto addTransactionUser(CreateTransactionDto dto) {
        UserDto user = UserContext.getCurrentUser();
        Finance financeUser = this.find(user.financeId());

        if (dto.typeTransaction().equals(TypeTransaction.EXPENSE) &&
                transactionAmountExceedsBalance(user.email(), dto.amount())) {

            BigDecimal diff = dto.amount().subtract(financeUser.getCurrentSavings());
            System.out.println("Внимание! Ваших накоплений не хватает для проведения транзакции. Пополните баланс на: " + diff);
            throw new LimitAmountBalance("Amount translation limit exceeded");
        }

        TransactionDto transaction = transactionService.create(dto, financeUser.getId());
        financeUser.getTransactionsId().add(transaction.id());

        updateCurrentSavings(financeUser, transaction.amount(), transaction.typeTransaction());

        financeRepository.save(financeUser);
        return transaction;
    }

    private void updateCurrentSavings(Finance finance, BigDecimal amount, TypeTransaction type) {
        BigDecimal currentSavings = finance.getCurrentSavings();
        if (type == TypeTransaction.PROFIT) {
            currentSavings = currentSavings.add(amount);
        } else if (type == TypeTransaction.EXPENSE) {
            currentSavings = currentSavings.subtract(amount);
        }
        finance.setCurrentSavings(currentSavings);
    }

    @Override
    public Boolean checkMonthlyExpenseLimit(String email) {
        UserDto user = userService.getUserByEmail(email);
        Finance finance = find(user.financeId());
        BigDecimal monthlyExpenses = getTotalExpenses(LocalDate.now().minusMonths(1), LocalDate.now(), email);

        if (monthlyExpenses.compareTo(finance.getMonthlyBudget()) > 0) {
            notificationService.sendMessage(UserContext.getCurrentUser().email(),
                    "Внимание! Вы превысили ваш месячный бюджет в " + finance.getMonthlyBudget() + "!");
            return true;
        }
        return false;
    }

    @Override
    public Boolean transactionAmountExceedsBalance(String email, BigDecimal transactionAmount) {
        UserDto user = userService.getUserByEmail(email);
        Finance finance = find(user.financeId());
        return finance.getCurrentSavings().compareTo(transactionAmount) < 0;
    }

    private Finance find(Long id) {
        return financeRepository.findById(id).orElseThrow(() -> new NotFoundException("Finance not found with id: " + id));
    }

    @Override
    public Double getProgressTowardsGoal(String email) {
        FinanceDto finance = getFinance(email);
        BigDecimal current = finance.currentSavings();
        BigDecimal goal = finance.savingsGoal();
        if (goal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return current.divide(goal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Override
    public List<TransactionDto> filterTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction, String email) {
        return transactionService.getFilteredTransactions(financeId, startDate, endDate, category, typeTransaction);
    }

    @Override
    public Map<String, BigDecimal> getExpensesByCategory(String email) {
        FinanceDto finance = getFinance(email);
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(TransactionDto::category,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDto::amount, BigDecimal::add)));
    }

    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    @Override
    public BigDecimal getTotalProfit(LocalDate startDate, LocalDate endDate, String email) {
        FinanceDto finance = getFinance(email);
        return getTotal(finance, startDate, endDate, TypeTransaction.PROFIT);
    }

    @Override
    public BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate, String email) {
        FinanceDto finance = getFinance(email);
        return getTotal(finance, startDate, endDate, TypeTransaction.EXPENSE);
    }

    private BigDecimal getTotal(FinanceDto finance, LocalDate startDate, LocalDate endDate, TypeTransaction typeTransaction) {
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == typeTransaction)
                .filter(t -> isWithinDateRange(t.date(), startDate, endDate))
                .map(TransactionDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean removeTransactionUser(Long id) {
        UserDto user = userService.getUserByEmail(UserContext.getCurrentUser().email());
        try {
            transactionService.delete(id);
            Finance finance = this.find(user.financeId());
            finance.getTransactionsId().remove(id);
            financeRepository.save(finance);
            log.debug("Removed transaction with id: {}", id);
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
    public List<TransactionDto> getTransactions(String userId) {
        UserDto user = userService.getUserByEmail(userId);
        List<Transaction> transactions = transactionService.getTransactionsByFinanceId(user.financeId());
        return transactionMapper.toDtoList(transactions);
    }

    @Override
    public FinanceDto getFinanceById(Long id) {
        return financeMapper.toDto(this.find(id));
    }

    @Override
    public Finance findFinanceById(Long id) {
        return this.find(id);
    }

    private FinanceDto getFinance(String email) {
        UserDto user = userService.getUserByEmail(email);
        Finance finance = find(user.financeId());
        List<Transaction> transactions = transactionService.getTransactionsByFinanceId(finance.getId());
        finance.setTransactionsId(transactions.stream().map(Transaction::getId).collect(Collectors.toList()));
        return financeMapper.toDto(finance);
    }
}
