package test.controller;

import app.controller.FinanceController;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.service.FinanceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinanceControllerTest {

    private MockMvc mockMvc;
    private FinanceController financeController;
    private User user;
    private UserContext userContext;
    private ObjectMapper objectMapper;

    @Mock
    private FinanceService financeService;

    @BeforeEach
    public void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.financeController = new FinanceController(financeService, Mappers.getMapper(TransactionMapper.class));
        this.mockMvc = MockMvcBuilders.standaloneSetup(financeController).build();
        this.user = new User(1L, "name", "email@mail.ru", "password", true, Role.USER, 1L);
        this.userContext = new UserContext();
        userContext.setUser(user);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createSuccess() throws Exception {
        Instant instant = Instant.now();

        CreateTransactionDto createTransactionDto = CreateTransactionDto.builder()
                .amount(BigDecimal.valueOf(100))
                .category("category")
                .description("description")
                .typeTransaction(TypeTransaction.PROFIT)
                .build();

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .category("category")
                .description("description")
                .typeTransaction(TypeTransaction.PROFIT)
                .date(instant)
                .build();


        when(financeService.createTransaction(eq(1L), any(Transaction.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(createTransactionDto))) // Используем DTO для запроса
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transaction.getId()))
                .andExpect(jsonPath("$.category").value(transaction.getCategory()))
                .andExpect(jsonPath("$.description").value(transaction.getDescription()))
                .andExpect(jsonPath("$.amount").value(transaction.getAmount()))
                .andExpect(jsonPath("$.date").value(transaction.getDate().truncatedTo(ChronoUnit.MILLIS).toString()))
                .andExpect(jsonPath("$.financeId").value(transaction.getFinanceId()));
    }


    @Test
    public void createFail() throws Exception {
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(BigDecimal.valueOf(-100), "category", "description", TypeTransaction.PROFIT);

        mockMvc.perform(post("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(createTransactionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete() throws Exception {

        Long idTransaction = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .param("id", String.valueOf(idTransaction)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update() throws Exception {

        Instant instant = Instant.now();

        UpdateTransactionDto updateTransactionDto = UpdateTransactionDto.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(500))
                .category("category")
                .description("description")
                .typeTransaction(TypeTransaction.PROFIT)
                .date(instant)
                .build();

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .category("category")
                .description("description")
                .typeTransaction(TypeTransaction.PROFIT)
                .date(instant)
                .build();


        TransactionDto transactionDto = TransactionDto.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .category("category")
                .description("description")
                .typeTransaction(TypeTransaction.PROFIT)
                .date(instant)
                .build();


        when(financeService.editTransaction(eq(1L), any(Transaction.class))).thenReturn(transaction);

        mockMvc.perform(put("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(updateTransactionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId()))
                .andExpect(jsonPath("$.category").value(transaction.getCategory()))
                .andExpect(jsonPath("$.description").value(transaction.getDescription()))
                .andExpect(jsonPath("$.amount").value(transaction.getAmount()))
                .andExpect(jsonPath("$.date").value(transaction.getDate().truncatedTo(ChronoUnit.MILLIS).toString()))
                .andExpect(jsonPath("$.financeId").value(transaction.getFinanceId()));
    }

    @Test
    void updateFail() throws Exception {
        UpdateTransactionDto updateTransactionDto = new UpdateTransactionDto(null, BigDecimal.valueOf(50), "category2", Instant.now().truncatedTo(ChronoUnit.MILLIS), "description2", TypeTransaction.EXPENSE);

        mockMvc.perform(put("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(updateTransactionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listFilterTransaction() throws Exception {
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        List<Transaction> transactionDtos = List.of(transaction);
        FilterTransactionDto filterTransactionDto = new FilterTransactionDto(
                Instant.now().minus(Duration.ofDays(1L)),
                Instant.now(),
                "category",
                "INCOME"
        );

        when(financeService.filterTransactions(anyLong(), eq(filterTransactionDto))).thenReturn(transactionDtos);

        mockMvc.perform(post("/api/v1/transaction/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(filterTransactionDto)))
                .andExpect(status().isOk());
    }


    @Test
    void listAll() throws Exception {
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(150.00))
                .category("Groceries")
                .description("Supermarket purchase")
                .typeTransaction(TypeTransaction.EXPENSE)
                .build();

        Set<Transaction> transactionDtos = Set.of(transaction);
        FilterTransactionDto filterTransactionDto = new FilterTransactionDto(Instant.now().minus(Duration.ofDays(1L)), Instant.now(), "category", TypeTransaction.PROFIT.name());

        when(financeService.list(anyLong())).thenReturn(transactionDtos);

        mockMvc.perform(get("/api/v1/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(filterTransactionDto)))
                .andExpect(status().isOk());
    }

    private String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}