package test.unit;

import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.entity.*;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.impl.TransactionServiceImpl;
import neket27.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    private Transaction transaction;

    private TransactionDto transactionDto;

    private UserContext userContext;

    private final Long financeId = 1L;

    @BeforeEach
    void setUp() {
        this.transaction = new Transaction(1L, BigDecimal.valueOf(100), "category", Instant.now(), "description", TypeTransaction.PROFIT, financeId);
        this.transactionDto = new TransactionDto(1L, BigDecimal.valueOf(100), "category", Instant.now(), "description", TypeTransaction.PROFIT, financeId);
        this.userContext = new UserContext();

        User userDto = User.builder()
                .id(1L)
                .name("name")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .isActive(true)
                .financeId(financeId)
                .build();

        userContext.setUser(userDto);
    }

    @Test
    void create() {
        Transaction lunchTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now())
                .financeId(financeId)
                .build();

        when(transactionRepository.save(lunchTransaction)).thenReturn(lunchTransaction);
        Transaction returnTransaction = transactionService.create(financeId, lunchTransaction);

        assertEquals(lunchTransaction, returnTransaction);
    }

    @Test
    void getTransactionById() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getTransactionById(transaction.getId());

        assertEquals(transaction, result);
    }

    @Test
    void edit() {
        Transaction updatedTransaction = new Transaction(1L, BigDecimal.valueOf(200), "newCategory", Instant.now(), "newDescription", TypeTransaction.EXPENSE, financeId);
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(updatedTransaction);

        Transaction returnUpdatedTransaction = transactionService.edit(updatedTransaction);

        assertEquals(updatedTransaction, returnUpdatedTransaction);
    }

    @Test
    void delete() {
        doNothing().when(transactionRepository).deleteById(transaction.getId());

        transactionService.delete(transaction.getId());

        // Assuming delete operation doesn't throw exception if successful
        assertDoesNotThrow(() -> transactionService.delete(transaction.getId()));
    }

    @Test
    void findAll() {
        List<Transaction> transactionDtos = List.of(transaction);
        Finance finance = new Finance(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(2000), List.of(1L));

        when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.of(transaction));
        List<Transaction> result = transactionService.findAll(finance);

        assertEquals(transactionDtos, result);
    }

    @Test
    void getFilteredTransactions() {
        FilterTransactionDto filterDto = new FilterTransactionDto(transaction.getDate().minusSeconds(100), Instant.now(), "category", "PROFIT");

        when(transactionRepository.getFilteredTransactions(financeId, filterDto.startDate(), filterDto.endDate(), filterDto.category(), filterDto.typeTransaction()))
                .thenReturn(List.of(transaction));

        List<Transaction> filteredTransactions = transactionService.getFilteredTransactions(filterDto);

        assertNotNull(filteredTransactions);
        assertFalse(filteredTransactions.isEmpty());
    }

    @Test
    void getTransactionsByFinanceId() {
        Set<Transaction> transactions = Set.of(transaction);

        when(transactionRepository.findByFinanceId(financeId)).thenReturn(List.of(transaction));

        Set<Transaction> result = transactionService.getTransactionsByFinanceId(financeId);

        assertEquals(transactions, result);
    }

}