package app.controller;

import app.context.UserContext;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping
    public TransactionDto create(@RequestBody CreateTransactionDto dto) {
        return financeService.addTransaction(getFinanceIdCurrentUser(),dto);
    }

    @DeleteMapping
    public void delete(@RequestParam Long id) {
        financeService.delete(getFinanceIdCurrentUser(),id);
    }

    @PutMapping
    public TransactionDto update(@RequestBody UpdateTransactionDto dto) {
        return financeService.editTransaction(getFinanceIdCurrentUser(), dto);
    }

    @PostMapping("/filter")
    public List<TransactionDto> listFilterTransaction(@RequestBody FilterTransactionDto dto) {
        return financeService.filterTransactions(getFinanceIdCurrentUser(), dto);
    }

    @GetMapping
    public Set<TransactionDto> listAll() {
        return financeService.getTransactions(getFinanceIdCurrentUser());
    }

    private Long getFinanceIdCurrentUser() {
        return UserContext.getCurrentUser().financeId();
    }

}
