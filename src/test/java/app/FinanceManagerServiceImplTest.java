package app;

import app.context.UserContext;
import app.entity.Finance;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.service.FinanceManagerService;
import app.service.UserService;
import app.service.impl.FinanceManagerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceManagerServiceImplTest {

    @Mock
    private UserService userService;


    private FinanceManagerService financeManagerService;

    private User user;
    private Finance finance;

    @BeforeEach
    void setUp() {
        this.finance = new Finance(new ArrayList<>());
        this.user = new User.Builder()
                .setFinance(finance)
                .setEmail("test@example.com")
                .build();

        this.financeManagerService = new FinanceManagerServiceImpl(userService);
        UserContext.setCurrentUser(user);
    }

    @Test
    void testSetMonthlyBudget() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        financeManagerService.setMonthlyBudget(5000);
        assertEquals(5000, user.getFinance().getMonthlyBudget());
    }

    @Test
    void testAddExpense() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        financeManagerService.addExpense(1000);
        assertEquals(1000, user.getFinance().getTotalExpenses());
    }

    @Test
    void testSetSavingsGoal() {
        financeManagerService.setSavingsGoal(10000);
        assertEquals(10000, user.getFinance().getSavingsGoal());
    }

    @Test
    void testAddTransaction() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        financeManagerService.addTransaction(200, "Food", Instant.now(), "Lunch", TypeTransaction.EXPENSE, "test@example.com");
        assertEquals(1, user.getFinance().getTransactions().size());
    }

    @Test
    void testEditTransaction() {
        user.getFinance().getTransactions().add(new Transaction(500, "Transport", Instant.now(), "Bus ticket", TypeTransaction.EXPENSE));
        financeManagerService.editTransaction(0, 700, "Taxi", "Cab fare");
        Transaction transaction = user.getFinance().getTransactions().get(0);
        assertEquals(700, transaction.getAmount());
        assertEquals("Taxi", transaction.getCategory());
        assertEquals("Cab fare", transaction.getDescription());
    }

    @Test
    void testDeleteTransaction() {
        user.getFinance().getTransactions().add(new Transaction(300, "Entertainment", Instant.now(), "Movie", TypeTransaction.EXPENSE));
        financeManagerService.deleteTransaction(0);
        assertTrue(user.getFinance().getTransactions().isEmpty());
    }

    @Test
    void testGetTotalExpenses() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        user.getFinance().getTransactions().add(new Transaction(200, "Food", Instant.now(), "Lunch", TypeTransaction.EXPENSE));
        user.getFinance().getTransactions().add(new Transaction(500, "Transport", Instant.now(), "Taxi", TypeTransaction.EXPENSE));
        double total = financeManagerService.getTotalExpenses(LocalDate.now().minusDays(1), LocalDate.now(), "test@example.com");
        assertEquals(700, total);
    }

    @Test
    void testGetExpensesByCategory() {
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        user.getFinance().getTransactions().add(new Transaction(200, "Food", Instant.now(), "Lunch", TypeTransaction.EXPENSE));
        user.getFinance().getTransactions().add(new Transaction(500, "Food", Instant.now(), "Dinner", TypeTransaction.EXPENSE));
        user.getFinance().getTransactions().add(new Transaction(300, "Transport", Instant.now(), "Taxi", TypeTransaction.EXPENSE));
        Map<String, Double> expenses = financeManagerService.getExpensesByCategory("test@example.com");
        assertEquals(700, expenses.get("Food"));
        assertEquals(300, expenses.get("Transport"));
    }
}
