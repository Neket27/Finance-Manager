package test.unit;

import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    private app.entity.Transaction transaction;

    private TransactionDto transactionDto;

    private UserContext userContext;

    private final Long financeId = 1L;

    @BeforeEach
    void setUp() {
        this.transaction = new app.entity.Transaction(1L, BigDecimal.valueOf(100), "category", Instant.now(), "description", TypeTransaction.PROFIT, financeId);
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
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(BigDecimal.valueOf(100), "category", "description", TypeTransaction.PROFIT);

        Transaction lunchTransaction = Transaction.builder()
                .amount(BigDecimal.valueOf(1000))
                .category("Food")
                .description("Lunch")
                .typeTransaction(TypeTransaction.EXPENSE)
                .date(Instant.now()) // или конкретная дата, если нужно
                .financeId(financeId)
                .build();

        when(transactionRepository.save(transaction)).thenReturn(transaction);
        Transaction returnTransaction = transactionService.create(financeId, lunchTransaction);

        assertEquals(transaction, returnTransaction);

    }

    @Test
    void getTransactionById() {
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        TransactionDto result = transactionService.getTransactionById(transaction.getId());

        assertEquals(transactionDto, result);
    }

    @Test
    void edit() {
        UpdateTransactionDto updateTransactionDto = new UpdateTransactionDto(transaction.getId(), BigDecimal.valueOf(200), "newCategory", Instant.now(), "newDescription", TypeTransaction.EXPENSE);
        TransactionDto updatedTransactionDto = new TransactionDto(1L, updateTransactionDto.amount(), updateTransactionDto.category(), updateTransactionDto.date(), updateTransactionDto.description(), updateTransactionDto.typeTransaction(), financeId);
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(transactionMapper.updateEntity(transaction, updateTransactionDto)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(updatedTransactionDto);

        TransactionDto returnUpdatedTransaction = transactionService.edit(updateTransactionDto);

        assertEquals(updatedTransactionDto, returnUpdatedTransaction);
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
        List<TransactionDto> transactionDtos = List.of(transactionDto);
        FinanceDto financeDto = new FinanceDto(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(2000), List.of(1L));

        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);
        when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.of(transaction));
        List<TransactionDto> result = transactionService.findAll(financeDto);

        assertEquals(transactionDtos, result);
    }

    @Test
    void getFilteredTransactions() {
        FilterTransactionDto filterDto = new FilterTransactionDto(transaction.getDate().minusSeconds(100), Instant.now(), "category", "PROFIT");

        when(transactionRepository.getFilteredTransactions(financeId, filterDto.startDate(), filterDto.endDate(), filterDto.category(), filterDto.typeTransaction()))
                .thenReturn(List.of(transaction));

        when(transactionMapper.toDtoList(List.of(transaction))).thenReturn(List.of(transactionDto));

        List<TransactionDto> filteredTransactions = transactionService.getFilteredTransactions(filterDto);

        assertNotNull(filteredTransactions);
        assertFalse(filteredTransactions.isEmpty());
    }

    @Test
    void getTransactionsByFinanceId() {
        Set<TransactionDto> transactionDtos = Set.of(transactionDto);

        when(transactionRepository.findByFinanceId(financeId)).thenReturn(List.of(transaction));
        when(transactionMapper.toDtoSet(List.of(transaction))).thenReturn(transactionDtos);

        Set<TransactionDto> result = transactionService.getTransactionsByFinanceId(financeId);

        assertEquals(transactionDtos, result);
    }

}