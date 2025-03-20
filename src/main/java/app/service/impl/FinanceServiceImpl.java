package app.service.impl;

import app.dto.finance.FinanceDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.exception.EditException;
import app.mapper.FinanceMapper;
import app.repository.FinanceRepository;
import app.service.FinanceService;
import app.service.NotificationService;
import app.context.UserContext;
import app.dto.transaction.CreateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.NotFoundException;
import app.mapper.TransactionMapper;
import app.service.TransactionService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация сервиса управления финансами.
 */
public class FinanceServiceImpl implements FinanceService {

    private final Logger log = LoggerFactory.getLogger(FinanceServiceImpl.class);
    private final FinanceRepository financeRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final FinanceMapper financeMapper;
    private final TransactionMapper transactionMapper;
    private final NotificationService notificationService;

    /**
     * Конструктор сервиса финансов.
     *
     * @param financeRepository   репозиторий финансов
     * @param userService         сервис пользователей
     * @param transactionService  сервис транзакций
     * @param financeMapper       маппер финансов
     * @param transactionMapper   маппер транзакций
     * @param notificationService сервис уведомлений
     */
    public FinanceServiceImpl(FinanceRepository financeRepository, UserService userService, TransactionService transactionService, FinanceMapper financeMapper, TransactionMapper transactionMapper, NotificationService notificationService) {
        this.financeRepository = financeRepository;
        this.userService = userService;
        this.transactionService = transactionService;
        this.financeMapper = financeMapper;
        this.transactionMapper = transactionMapper;
        this.notificationService = notificationService;
    }

    /**
     * Добавляет транзакцию пользователю.
     *
     * @param dto данные для создания транзакции
     * @return созданная транзакция
     */
    @Override
    public TransactionDto addTransactionUser(CreateTransactionDto dto) {
        Finance financeUser = this.find(UserContext.getCurrentUser().financeId());
        TransactionDto transaction = transactionService.create(dto, financeUser.getId());
        financeUser.getTransactionsId().add(transaction.id());

        this.updateCurrentSavings(financeUser, transaction.amount(), transaction.typeTransaction());

        financeRepository.save(financeUser);
        return transaction;
    }

    /**
     * Обновляет текущие накопления пользователя в зависимости от типа транзакции.
     *
     * @param finance финансовые данные пользователя
     * @param amount  сумма транзакции
     * @param type    тип транзакции (доход/расход)
     */
    private void updateCurrentSavings(Finance finance, double amount, TypeTransaction type) {

        if (type == TypeTransaction.PROFIT) {
            finance.setCurrentSavings(finance.getCurrentSavings() + amount);
        } else if (type == TypeTransaction.EXPENSE) {
            finance.setCurrentSavings(finance.getCurrentSavings() - amount);
            if (finance.getCurrentSavings() < 0) {
                finance.setCurrentSavings(0);
                System.out.println("Внимание! Ваши накопления стали отрицательными. Установите новую цель или пересмотрите расходы.");
            }
        }
    }

    /**
     * Проверяет превышение месячного бюджета.
     *
     * @param email email пользователя
     */
    @Override
    public Boolean checkExpenseLimit(String email) {
        UserDto user = userService.getUserByEmail(email);
        Finance finance = this.find(user.financeId());

        if (finance.getTotalExpenses() > finance.getMonthlyBudget()) {
            notificationService.sendMessage(UserContext.getCurrentUser().email(), "Внимание! Вы превысили ваш месячный бюджет в" + finance.getMonthlyBudget() + "!");
            return true;
        }
        return false;
    }


    private Finance find(Long id) {
        return financeRepository.findById(id).orElseThrow(() -> new NotFoundException("Finance not found with id: " + id));
    }


    /**
     * Рассчитывает процент выполнения накоплений относительно цели.
     *
     * @param email email пользователя
     * @return процент выполнения цели
     */
    @Override
    public double getProgressTowardsGoal(String email) {
        FinanceDto finance = getFinance(email);
        return (finance.currentSavings() / finance.savingsGoal()) * 100;
    }

    /**
     * Фильтрует транзакции по заданным параметрам.
     *
     * @param startDate       начальная дата
     * @param endDate         конечная дата
     * @param category        категория
     * @param typeTransaction тип транзакции (доход/расход)
     * @param email           email пользователя
     * @return отфильтрованный список транзакций
     */
    @Override
    public List<TransactionDto> filterTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction, String email) {
        return transactionService.getFilteredTransactions(financeId, startDate, endDate, category, typeTransaction);

    }


    @Override
    public Map<String, Double> getExpensesByCategory(String email) {
        FinanceDto finance = getFinance(email);
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(TransactionDto::category, Collectors.summingDouble(TransactionDto::amount)));
    }


    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    @Override
    public double getTotalIncome(LocalDate startDate, LocalDate endDate, String email) {
        FinanceDto finance = getFinance(email);
        return getTotal(finance, startDate, endDate, TypeTransaction.PROFIT);
    }

    @Override
    public double getTotalExpenses(LocalDate startDate, LocalDate endDate, String email) {
        FinanceDto finance = getFinance(email);
        return getTotal(finance, startDate, endDate, TypeTransaction.EXPENSE);
    }


    private double getTotal(FinanceDto finance, LocalDate startDate, LocalDate endDate, TypeTransaction typeTransaction) {
        return finance.transactionsId().stream()
                .map(transactionService::getTransactionById)
                .filter(t -> t.typeTransaction() == typeTransaction)
                .filter(t -> isWithinDateRange(t.date(), startDate, endDate))
                .mapToDouble(TransactionDto::amount)
                .sum();
    }

    /**
     * Удаляет транзакцию пользователя.
     *
     * @param id идентификатор транзакции
     * @return true, если удаление прошло успешно, иначе false
     */
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

    /**
     * Редактирует существующую транзакцию.
     *
     * @param updateTransactionDto данные для обновления транзакции
     * @return обновленная транзакция
     * @throws EditException если редактирование не удалось
     */
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

    /**
     * Получает финансовые данные пользователя.
     *
     * @param email email пользователя
     * @return объект финансовых данных
     */
    private FinanceDto getFinance(String email) {
        UserDto user = userService.getUserByEmail(email);
        Finance finance = find(user.financeId());
        List<Transaction> transactions = transactionService.getTransactionsByFinanceId(finance.getId());
        finance.setTransactionsId(transactions.stream().map(Transaction::getId).collect(Collectors.toList()));
        return financeMapper.toDto(finance);
    }

}
