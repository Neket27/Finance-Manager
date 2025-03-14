package app;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exeption.NotFoundException;
import app.mapper.TransactionMapper;
import app.repository.TransactionRepository;
import app.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;
    private TransactionDto transactionDto;
    private CreateTransactionDto createTransactionDto;
    private UpdateTransactionDto updateTransactionDto;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDate(Instant.now());
        transaction.setCategory("Food");
        transaction.setTypeTransaction(TypeTransaction.EXPENSE);

        transactionDto = new TransactionDto(1L, 100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE);
        createTransactionDto = new CreateTransactionDto(100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE);
        updateTransactionDto = new UpdateTransactionDto(1L, 100.0, "Food", Instant.now(), "", TypeTransaction.EXPENSE);
    }

    @Test
    void testCreateTransaction() {
        when(transactionMapper.toEntity(any(CreateTransactionDto.class))).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        TransactionDto result = transactionService.create(createTransactionDto);

        assertNotNull(result);
        assertEquals("Food", result.category());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        TransactionDto result = transactionService.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void testGetTransactionById_NotFound() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.getTransactionById(1L));
    }

    @Test
    void testEditTransaction() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.edit(updateTransactionDto);

        assertNotNull(result);
        assertEquals("Food", result.getCategory());
    }

    @Test
    void testDeleteTransaction() {
        doNothing().when(transactionRepository).deleteById(anyLong());

        boolean result = transactionService.delete(1L);

        assertTrue(result);
        verify(transactionRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void testFindAllTransactions() {
        FinanceDto financeDto = new FinanceDto.Builder()
                .transactionsId(List.of(1L))
                .build();
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        List<TransactionDto> transactions = transactionService.findAll(financeDto);

        assertFalse(transactions.isEmpty());
    }

    @Test
    void testGetFilteredTransactions() {
        List<Long> transactionIds = List.of(1L);
        Instant startDate = Instant.now().minusSeconds(3600);
        Instant endDate = Instant.now().plusSeconds(3600);

        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(transactionDto);

        List<TransactionDto> transactions = transactionService.getFilteredTransactions(transactionIds, startDate, endDate, "Food", TypeTransaction.EXPENSE);

        assertEquals(1, transactions.size());
    }
}