package app.service.impl;

import app.dto.transaction.FilterTransactionDto;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.User;
import app.exception.common.CreateException;
import app.exception.common.DeleteException;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.TransactionService;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neket27.context.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
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


    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Transaction create(Long financeId, Transaction transaction) {
        try {
            transaction.setDate(Instant.now());
            transaction.setFinanceId(financeId);
            transaction = transactionRepository.save(transaction);

            log.debug("addTransaction: {}", transaction.toString());
            return transaction;
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
    public Transaction getTransactionById(Long id) {
        return find(id);
    }

    /**
     * Ищет транзакцию по идентификатору.
     *
     * @param id идентификатор транзакции
     * @return найденная транзакция
     */
    private app.entity.Transaction find(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionException("Transaction not found with id: " + id) {
        });
    }

    @Override
    @Auditable
    @Transactional(rollbackFor = Exception.class)
    public Transaction edit(Transaction updatedTransaction) {
        Transaction transaction = this.find(updatedTransaction.getId());
        transactionMapper.updateEntity(transaction, updatedTransaction);
        transaction = transactionRepository.save(transaction);
        log.debug("Транзакция обновлена: {}", transaction);
        return transaction;
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
    public List<Transaction> findAll(Finance finance) {
        return finance.getTransactionsId().stream().map(this::getTransactionById).toList();
    }

    /**
     * Фильтрует транзакции по заданным параметрам.
     */
    @Override
    @Auditable
    @Transactional
    public List<Transaction> getFilteredTransactions(FilterTransactionDto f) {
        User user = (User) UserContext.getCurrentUser();
        return transactionRepository.getFilteredTransactions(user.getFinanceId(),
                f.startDate(), f.endDate(), f.category(), f.typeTransaction());
    }

    @Override
    @Auditable
    @Transactional
    public Set<Transaction> getTransactionsByFinanceId(Long id) {
        return new HashSet<>(transactionRepository.findByFinanceId(id));
    }

}
