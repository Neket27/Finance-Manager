package app.service.impl;

import app.container.Component;
import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Transaction;
import app.exception.NotFoundException;
import app.exception.TransactionException;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * Реализация сервиса управления транзакциями.
 */

@Component
public class TransactionServiceImpl implements TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Конструктор сервиса транзакций.
     *
     * @param transactionRepository репозиторий транзакций
     * @param transactionMapper     маппер транзакций
     */
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Создает новую транзакцию.
     *
     * @param dto данные для создания транзакции
     * @return созданная транзакция
     */
    @Override
    public TransactionDto create(CreateTransactionDto dto) {
        try {
            UserDto user = UserContext.getCurrentUser();

            Transaction transaction = transactionMapper.toEntity(dto);
            transaction.setDate(Instant.now());
            transaction.setFinanceId(user.financeId());
            transaction = transactionRepository.save(transaction);

            log.debug("addTransaction: {}", transaction.toString());
            return transactionMapper.toDto(transaction);
        } catch (Exception e) {
            throw new TransactionException("Error adding transaction", e);
        }
    }

    /**
     * Получает транзакцию по её идентификатору.
     *
     * @param id идентификатор транзакции
     * @return объект транзакции
     */
    @Override
    public TransactionDto getTransactionById(Long id) {
        return transactionMapper.toDto(find(id));
    }

    /**
     * Ищет транзакцию по идентификатору.
     *
     * @param id идентификатор транзакции
     * @return найденная транзакция
     * @throws NotFoundException если транзакция не найдена
     */
    private Transaction find(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction not found with id: " + id));
    }

    /**
     * Редактирует существующую транзакцию.
     *
     * @param dto данные для обновления транзакции
     * @return обновленная транзакция
     */
    @Override
    public TransactionDto edit(UpdateTransactionDto dto) {
        Transaction transaction = this.find(dto.id());
        transactionMapper.updateEntity(transaction, dto);
        transactionRepository.save(transaction);
        log.debug("Транзакция обновлена: {}", transaction);
        return transactionMapper.toDto(transaction);
    }

    /**
     * Удаляет транзакцию по её идентификатору.
     *
     * @param id идентификатор транзакции
     * @return true, если удаление прошло успешно, иначе false
     */
    @Override
    public void delete(Long id) {
        try {
            transactionRepository.deleteById(id);
            log.debug("deleteTransaction with id: {}", id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TransactionException("Error deleting transaction", e);
        }
    }

    /**
     * Получает все транзакции пользователя.
     *
     * @param finance финансовые данные пользователя
     * @return список транзакций
     */
    @Override
    public List<TransactionDto> findAll(FinanceDto finance) {
        return finance.transactionsId().stream().map(this::getTransactionById).toList();
    }

    /**
     * Фильтрует транзакции по заданным параметрам.
     */
    @Override
    public List<TransactionDto> getFilteredTransactions(FilterTransactionDto f) {
        UserDto user = UserContext.getCurrentUser();
        return transactionMapper.toDtoList(transactionRepository.getFilteredTransactions(user.financeId(),
                f.startDate(), f.endDate(), f.category(), f.typeTransaction()));
    }

    @Override
    public List<TransactionDto> getTransactionsByFinanceId(Long id) {
        return transactionMapper.toDtoList(transactionRepository.findByFinanceId(id));
    }

}
