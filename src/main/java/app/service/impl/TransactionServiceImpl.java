package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.context.UserContext;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Transaction;
import app.exception.common.CreateException;
import app.exception.common.DeleteException;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Реализация сервиса управления транзакциями.
 */

@Slf4j
@Service
@CustomLogging
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;


    /**
     * Создает новую транзакцию.
     *
     * @param dto данные для создания транзакции
     * @return созданная транзакция
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public TransactionDto create(Long financeId, CreateTransactionDto dto) {
        try {
            Transaction transaction = transactionMapper.toEntity(dto);
            transaction.setDate(Instant.now());
            transaction.setFinanceId(financeId);
            transaction = transactionRepository.save(transaction);

            log.debug("addTransaction: {}", transaction.toString());
            return transactionMapper.toDto(transaction);
        } catch (Exception e) {
            throw new CreateException("Error create transaction", e);
        }
    }

    /**
     * Получает транзакцию по её идентификатору.
     *
     * @param id идентификатор транзакции
     * @return объект транзакции
     */
    @Override
    @Auditable
    @Transactional
    public TransactionDto getTransactionById(Long id) {
        return transactionMapper.toDto(find(id));
    }

    /**
     * Ищет транзакцию по идентификатору.
     *
     * @param id идентификатор транзакции
     * @return найденная транзакция
     */
    private Transaction find(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionException("Transaction not found with id: " + id) {
        });
    }

    /**
     * Редактирует существующую транзакцию.
     *
     * @param dto данные для обновления транзакции
     * @return обновленная транзакция
     */
    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
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
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        try {
            transactionRepository.deleteById(id);
            log.debug("deleteTransaction with id: {}", id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DeleteException("Error deleting transaction", e);
        }
    }

    /**
     * Получает все транзакции пользователя.
     *
     * @param finance финансовые данные пользователя
     * @return список транзакций
     */
    @Override
    @Auditable
    @Transactional
    public List<TransactionDto> findAll(FinanceDto finance) {
        return finance.transactionsId().stream().map(this::getTransactionById).toList();
    }

    /**
     * Фильтрует транзакции по заданным параметрам.
     */
    @Override
    @Auditable
    @Transactional
    public List<TransactionDto> getFilteredTransactions(FilterTransactionDto f) {
        UserDto user = UserContext.getCurrentUser();
        return transactionMapper.toDtoList(transactionRepository.getFilteredTransactions(user.financeId(),
                f.startDate(), f.endDate(), f.category(), f.typeTransaction()));
    }

    @Override
    @Auditable
    @Transactional
    public Set<TransactionDto> getTransactionsByFinanceId(Long id) {
        return transactionMapper.toDtoSet(transactionRepository.findByFinanceId(id));
    }

}
