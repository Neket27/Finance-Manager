package app.controller;

import app.context.UserContext;
import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@CustomExceptionHandler
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto create(@Valid @RequestBody CreateTransactionDto dto) {
        return financeService.createTransaction(getFinanceIdCurrentUser(),dto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(name = "id") Long id) {
        financeService.delete(getFinanceIdCurrentUser(),id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto update(@Valid @RequestBody UpdateTransactionDto dto) {
        return financeService.editTransaction(getFinanceIdCurrentUser(), dto);
    }

    @PostMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> listFilterTransaction(@Valid @RequestBody FilterTransactionDto dto) {
        return financeService.filterTransactions(getFinanceIdCurrentUser(), dto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<TransactionDto> listAll() {
        return financeService.list(getFinanceIdCurrentUser());
    }

    private Long getFinanceIdCurrentUser() {
        return UserContext.getCurrentUser().financeId();
    }

}
