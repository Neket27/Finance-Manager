package test.unit;

import app.context.UserContext;
import app.dto.finance.CreateFinanceDto;
import app.dto.finance.FinanceDto;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.exception.common.DeleteException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.service.NotificationService;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.FinanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceImplTest {

    @Mock
    private FinanceRepository financeRepository;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private FinanceMapper financeMapper;

    @InjectMocks
    private FinanceServiceImpl financeService;

    private UserDto userDto;
    private Finance finance;
    private FinanceDto financeDto;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto.Builder()
                .id(1L)
                .name("name")
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .finance(1L)
                .role(Role.USER)
                .build();

        finance = new Finance.Builder()
                .id(1L)
                .currentSavings(BigDecimal.valueOf(1000))
                .monthlyBudget(BigDecimal.valueOf(500))
                .totalExpenses(BigDecimal.valueOf(600))
                .transactionsId(new ArrayList<>(List.of(1L, 2L)))
                .build();

        UserContext.setCurrentUser(userDto);

        financeDto = new FinanceDto(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(2000), List.of(1L, 2L));
        transactionDto = new TransactionDto(1L, BigDecimal.valueOf(400), "Food", Instant.now(), "", TypeTransaction.EXPENSE, 1L);
    }


    @Test
    void createEmptyFinance() {
        CreateFinanceDto financeDto = new CreateFinanceDto(BigDecimal.valueOf(100), BigDecimal.valueOf(500), BigDecimal.valueOf(600), BigDecimal.valueOf(2000), List.of(1L));

        when(financeMapper.toEntity(financeDto)).thenReturn(finance);
        when(financeRepository.save(finance)).thenReturn(finance);

        Long idFinance = financeService.createEmptyFinance(financeDto);
        assertNotNull(idFinance);
    }

    @Test
    void createTransaction() {
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(BigDecimal.valueOf(100), "category", "description", TypeTransaction.EXPENSE);

        when(financeRepository.findById(anyLong())).thenReturn(Optional.ofNullable(finance));
        when(transactionService.create(anyLong(), any())).thenReturn(transactionDto);

        TransactionDto returnTransactionDto = financeService.createTransaction(1L, createTransactionDto);

        assertEquals(transactionDto, returnTransactionDto);
    }

    @Test
    void getExpensesByCategory() {

        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when( financeRepository.findById(anyLong())).thenReturn(Optional.ofNullable(finance));
        when(transactionService.getTransactionsByFinanceId(anyLong())).thenReturn(Set.of(transactionDto));
        when(financeMapper.toDto(finance)).thenReturn(financeDto);

        var expensesByCategory =financeService.getExpensesByCategory(finance.getId());

        assertEquals(Map.of(transactionDto.category(), BigDecimal.valueOf(800)), expensesByCategory);

    }

    @Test
    void delete() {
        Long financeId = 1L;
        Long transactionId = 1L;

        when(transactionService.getTransactionsByFinanceId(financeId)).thenReturn(Set.of(transactionDto));
        when(financeRepository.findById(financeId)).thenReturn(Optional.ofNullable(finance));
        when(financeRepository.findById(financeId)).thenReturn(Optional.of(finance));

        financeService.delete(financeId, transactionId);

        verify(transactionService).delete(transactionId);
        verify(financeRepository).save(finance);
    }

    @Test
    void delete_TransactionNotFound() {
        Long financeId = 1L;
        Long transactionId = 999L;

        when(transactionService.getTransactionsByFinanceId(financeId)).thenReturn(Set.of(transactionDto));

        assertThrows(DeleteException.class, () -> financeService.delete(financeId, transactionId));
    }

    @Test
    void updatetMonthlyBudget() {
        Long financeId = 1L;
        BigDecimal newBudget = BigDecimal.valueOf(1000);

        when(financeRepository.findById(financeId)).thenReturn(Optional.of(finance));

        financeService.updatetMonthlyBudget(financeId, newBudget);

        assertEquals(newBudget, finance.getMonthlyBudget());
        verify(financeRepository).save(finance);
    }

    @Test
    void filterTransactions() {
        FilterTransactionDto filter = new FilterTransactionDto(Instant.now(),Instant.now(),"category","PROFIT");

        when(transactionService.getFilteredTransactions(filter)).thenReturn(List.of(transactionDto));

        List<TransactionDto> result = financeService.filterTransactions(1L, filter);

        assertEquals(1, result.size());
        assertEquals(transactionDto, result.get(0));
    }

    @Test
    void getTotalProfit() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(financeRepository.findById(anyLong())).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionsByFinanceId(anyLong())).thenReturn(Set.of(transactionDto));
        when(financeMapper.toDto(finance)).thenReturn(financeDto);

        BigDecimal totalProfit = financeService.getTotalProfit(startDate, endDate, finance.getId());

        assertEquals(BigDecimal.ZERO, totalProfit);
    }

    @Test
    void getTotalExpenses() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(financeRepository.findById(anyLong())).thenReturn(Optional.of(finance));
        when(transactionService.getTransactionsByFinanceId(anyLong())).thenReturn(Set.of(transactionDto));
        when(financeMapper.toDto(finance)).thenReturn(financeDto);

        BigDecimal totalExpenses = financeService.getTotalExpenses(startDate, endDate, finance.getId());

        assertEquals(BigDecimal.valueOf(800), totalExpenses);
    }

    @Test
    void editTransaction() {
        UpdateTransactionDto updateTransactionDto = new UpdateTransactionDto(1L, BigDecimal.valueOf(10), "ct", Instant.now(), "d", TypeTransaction.EXPENSE);

        when(transactionService.getTransactionsByFinanceId(anyLong())).thenReturn(Set.of(transactionDto));
        when(transactionService.edit(any())).thenReturn(transactionDto);

        TransactionDto result = financeService.editTransaction(1L, updateTransactionDto);

        assertEquals(transactionDto, result);
    }

    @Test
    void save() {
        when(financeRepository.save(finance)).thenReturn(finance);

        Finance result = financeService.save(finance);

        assertEquals(finance, result);
    }

    @Test
    void list() {
        when(transactionService.getTransactionsByFinanceId(anyLong())).thenReturn(Set.of(transactionDto));

        Set<TransactionDto> result = financeService.list(1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(transactionDto));
    }

    @Test
    void getFinanceById() {
        when(financeRepository.findById(anyLong())).thenReturn(Optional.of(finance));
        when(financeMapper.toDto(finance)).thenReturn(financeDto);

        FinanceDto result = financeService.getFinanceById(1L);

        assertEquals(financeDto, result);
    }

    @Test
    void findFinanceById() {
        when(financeRepository.findById(anyLong())).thenReturn(Optional.of(finance));

        Finance result = financeService.findFinanceById(1L);

        assertEquals(finance, result);
    }

}