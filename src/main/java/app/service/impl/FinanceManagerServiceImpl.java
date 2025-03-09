package app.service.impl;

import app.context.UserContext;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.service.FinanceManagerService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceManagerServiceImpl implements FinanceManagerService {

    private final Logger log = LoggerFactory.getLogger(FinanceManagerServiceImpl.class);

    private final UserService userService;

    public FinanceManagerServiceImpl(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void setMonthlyBudget(double budget) {
        User user = UserContext.getCurrentUser();
        user.getFinance().setMonthlyBudget(budget);
        log.debug("Месячный бюджет установлен: {}", budget);
        checkExpenseLimit(user.getEmail());
    }

    @Override
    public void addExpense(double amount) {
        User user = UserContext.getCurrentUser();
        user.getFinance().setTotalExpenses(user.getFinance().getTotalExpenses() + amount);
        checkExpenseLimit(user.getEmail());
    }

    @Override
    public void setSavingsGoal(double goal) {
        User user = UserContext.getCurrentUser();
        user.getFinance().setSavingsGoal(goal);
        log.debug("Цель накопления установлена: {}", goal);
    }


    @Override
    public void addTransaction(double amount, String category, Instant date, String description, TypeTransaction typeTransaction, String email) {
        User user = UserContext.getCurrentUser();
        user.getFinance().getTransactions().add(new Transaction(amount, category, date, description, typeTransaction));
        checkBudgetExceeded(email);
        updateSavings(amount, typeTransaction);
    }

    @Override
    public void editTransaction(int index, double amount, String category, String description) {
        User user = UserContext.getCurrentUser();
        if (index >= 0 && index < user.getFinance().getTransactions().size()) {
            Transaction transaction = user.getFinance().getTransactions().get(index);
            transaction.setAmount(amount);
            transaction.setCategory(category);
            transaction.setDescription(description);
        }
    }

    @Override
    public void deleteTransaction(int index) {
        User user = UserContext.getCurrentUser();
        if (index >= 0 && index < user.getFinance().getTransactions().size()) {
            user.getFinance().getTransactions().remove(index);
        }
    }

    @Override
    public void checkBudgetExceeded(String email) {
        User user = userService.getUserByEmail(email);
        double totalExpenses = user.getFinance().getTransactions().stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlyBudget = user.getFinance().getMonthlyBudget();
        if (totalExpenses > monthlyBudget) {
            System.out.println("Внимание! Вы превысили месячный бюджет на " + (totalExpenses - monthlyBudget) + "!");
        }
    }

    @Override
    public void updateSavings(double amount, TypeTransaction type) {
        User user = UserContext.getCurrentUser();
        if (type == TypeTransaction.PROFIT) {
            user.getFinance().setCurrentSavings(user.getFinance().getSavingsGoal() + amount);// Увеличиваем накопления при доходе
        } else if (type == TypeTransaction.EXPENSE) {
            user.getFinance().setCurrentSavings(user.getFinance().getCurrentSavings() - amount);// Уменьшаем накопления при расходах
            if (user.getFinance().getCurrentSavings() < 0) {
                user.getFinance().setCurrentSavings(0); // Не допускаем отрицательных накоплений
                System.out.println("Внимание! Ваши накопления стали отрицательными. Установите новую цель или пересмотрите расходы.");
            }
        }
    }


    @Override
    public double getProgressTowardsGoal(String email) {
        User user = userService.getUserByEmail(email);
        return (user.getFinance().getCurrentSavings() / user.getFinance().getSavingsGoal()) * 100; // Процент выполнения цели
    }


    @Override
    public List<Transaction> filterTransactions(Instant startDate, Instant endDate, String category, TypeTransaction typeTransaction, String email) {
       User user = userService.getUserByEmail(email);
        return user.getFinance().getTransactions().stream()
                .filter(t -> (startDate == null || t.getDate().isAfter(startDate)) &&
                        (endDate == null || t.getDate().isBefore(endDate)) &&
                        (category == null || t.getCategory().equalsIgnoreCase(category)) &&
                        (typeTransaction == null || t.getTypeTransaction() == typeTransaction)
                )
                .collect(Collectors.toList());
    }

    //     Анализ расходов по категориям
    @Override
    public Map<String, Double> getExpensesByCategory(String email) {
        User user = userService.getUserByEmail(email);
        return user.getFinance().getTransactions().stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
    }


    private boolean isWithinDateRange(Instant date, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = date.atZone(ZoneId.systemDefault()).toLocalDate();
        return (transactionDate.isEqual(startDate) || transactionDate.isEqual(endDate) ||
                (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)));
    }

    // Расчёт суммарного дохода и расхода за определённый период
    @Override
    public double getTotalIncome(LocalDate startDate, LocalDate endDate, String email) {
        User user = userService.getUserByEmail(email);
        return user.getFinance().getTransactions().stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.PROFIT)
                .filter(t -> isWithinDateRange(t.getDate(), startDate, endDate))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public double getTotalExpenses(LocalDate startDate, LocalDate endDate, String email) {
        User user = userService.getUserByEmail(email);
        return user.getFinance().getTransactions().stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.EXPENSE)
                .filter(t -> isWithinDateRange(t.getDate(), startDate, endDate))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public void checkExpenseLimit(String email) {
        User user = userService.getUserByEmail(email);
        if (user.getFinance().getTotalExpenses() > user.getFinance().getMonthlyBudget()) {
            sendExpenseLimitNotification();
        }
    }

    // Формирование отчёта по финансовому состоянию
    @Override
    public void generateFinancialReport(String email) {
        User user = userService.getUserByEmail(email);
        System.out.println("==== Финансовый отчет ====");
        System.out.println("Текущие накопления: " + user.getFinance().getCurrentSavings());
        System.out.println("Цель накопления: " + user.getFinance().getSavingsGoal());
        System.out.println("Прогресс к цели: " + getProgressTowardsGoal(user.getEmail()) + "%");

        System.out.println("Суммарный доход за период: " + getTotalIncome(LocalDate.now().minusMonths(1), LocalDate.now(), user.getEmail()));
        System.out.println("Суммарные расходы за период: " + getTotalExpenses(LocalDate.now().minusMonths(1), LocalDate.now(), user.getEmail()));

        System.out.println("Расходы по категориям:");
        getExpensesByCategory(email).forEach((category, total) ->
                System.out.println(category + ": " + total));

        System.out.println("===========================");
    }

    private void sendExpenseLimitNotification() {
        User user = UserContext.getCurrentUser();
        System.out.println("Внимание! Вы превысили ваш месячный бюджет в " + user.getFinance().getMonthlyBudget() + "!");
        // TODO логика для отправки email-уведомления
    }

}


