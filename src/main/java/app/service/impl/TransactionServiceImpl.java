package app.service.impl;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.NotFoundException;
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
    public TransactionDto create(CreateTransactionDto dto, Long financeId) {
        try {
            Transaction transaction = transactionMapper.toEntity(dto);
            transaction.setFinanceId(financeId);
            transaction = transactionRepository.save(transaction);
            log.debug("addTransaction: {}", transaction.toString());
            return transactionMapper.toDto(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Error adding transaction", e);
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
    public boolean delete(Long id) {
        try {
            transactionRepository.deleteById(id);
            log.debug("deleteTransaction with id: {}", id);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
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
     *
     * @param financeId       id финансов пользователя
     * @param startDate       начальная дата
     * @param endDate         конечная дата
     * @param category        категория транзакции
     * @param typeTransaction тип транзакции (доход/расход)
     * @return отфильтрованный список транзакций
     */
    @Override
    public List<TransactionDto> getFilteredTransactions(Long financeId, Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction) {
        return transactionMapper.toDtoList(transactionRepository.getFilteredTransactions(financeId, startDate, endDate, category, typeTransaction));
    }

    @Override
    public List<Transaction> getTransactionsByFinanceId(Long id) {
        return transactionRepository.findByFinanceId(id);
    }

}
