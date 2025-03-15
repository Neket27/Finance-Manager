package app.util.out;

import app.context.UserContext;
import app.dto.transaction.CreateTransactionDto;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.TypeTransaction;
import app.service.FinanceService;
import app.service.TargetService;
import app.service.UserService;
import app.util.in.UserAuth;
import app.util.in.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Menu {

    private final Logger log = LoggerFactory.getLogger(Menu.class);
    private final UserOutput userOutput;
    private final UserInput userInput;
    private final FinanceService financeService;
    private final UserAuth userAuth;
    private final UserService userService;
    private final TargetService targetService;

    public Menu(UserOutput userOutput, UserInput userInput, FinanceService financeService, UserAuth userAuth, UserService userService, TargetService targetService) {
        this.userOutput = userOutput;
        this.userInput = userInput;
        this.financeService = financeService;
        this.userAuth = userAuth;
        this.userService = userService;
        this.targetService = targetService;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            displayMainMenu();

            int choice = userInput.readInt("Ваш выбор: ");

            switch (choice) {
                case 0:
                    userAuth.registerUser();
                    break;
                case 1:
                    handleLoginLogout();
                    break;
                case 2:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        addTransaction();
                    break;
                case 3:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        viewTransactions();
                    break;
                case 4:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        filterTransactions();
                    break;
                case 5:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        editTransaction();
                    break;
                case 6:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        deleteTransaction();
                    break;
                case 7:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        setSavingsGoal();
                    break;
                case 8:
                    trackProgress();
                    break;
                case 9:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        setMonthlyBudget();
                    break;
                case 10:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        generateFinancialReport();
                    break;
                case 11:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive())
                        blockUser();
                    break;
                case 12:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive() && isAdmin(UserContext.getCurrentUser()))
                        deleteUser();
                    break;
                case 13:
                    if (UserContext.getCurrentUser() != null && UserContext.getCurrentUser().isActive() && isAdmin(UserContext.getCurrentUser()))
                        assignUserRole();
                    break;
                case 14:
                    running = false;
                    userOutput.print("Вы вышли из системы.");
                    userInput.close();
                    break;
                default:
                    userOutput.print("Неверный выбор. Пожалуйста, попробуйте снова.");
                    break;
            }
        }
    }

    private void displayMainMenu() {
        userOutput.print("\nВыберите действие:");
        userOutput.print("0. Зарегистрировать пользователя");

        if (UserContext.getCurrentUser() == null) {
            userOutput.print("1. Войти");
        } else {
            if (UserContext.getCurrentUser().isActive()) {
                userOutput.print("1. Выйти из аккаунта");
                userOutput.print("2. Добавить транзакцию");
                userOutput.print("3. Просмотреть транзакции");
                userOutput.print("4. Фильтровать транзакции");
                userOutput.print("5. Редактировать транзакцию");
                userOutput.print("6. Удалить транзакцию");
                userOutput.print("7. Установить цель накопления");
                userOutput.print("8. Отслеживать прогресс по цели накопления");
                userOutput.print("9. Установить месячный бюджет");
                userOutput.print("10. Сгенерировать финансовый отчет");
                if (isAdmin(UserContext.getCurrentUser())) {
                    userOutput.print("11. Блокировка пользователя");
                    userOutput.print("12. Удаление пользователя");
                    userOutput.print("13. Назначить роль пользователю");
                }
            } else {
                userOutput.print("1. Выйти из аккаунта");
                userOutput.print("Здравствуйте, вы заблокированны!");
            }
            userOutput.print("14. Выход из системы");
        }
    }

    private void handleLoginLogout() {
        if (UserContext.getCurrentUser() == null) {
            userAuth.loginUser();
        } else {
            userAuth.logoutUser();
        }
    }

    private void addTransaction() {
        double amount = userInput.readDouble("Введите сумму транзакции: ");
        String category = userInput.readString("Введите категорию: ");
        String description = userInput.readString("Введите описание: ");

        TypeTransaction typeTransaction = getTypeTransactionFromUser();
        financeService.addTransactionUser(new CreateTransactionDto(amount, category, Instant.now(), description, typeTransaction));
        targetService.checkBudgetExceeded(UserContext.getCurrentUser().email());
        userOutput.print("Транзакция добавлена!");
    }

    private TypeTransaction getTypeTransactionFromUser() {
        userOutput.print("Введите тип транзакции (доход/расход): ");
        boolean correctInputTypeTransaction = false;
        TypeTransaction typeTransaction = null;

        while (!correctInputTypeTransaction) {
            String transaction = userInput.readString("Тип транзакции: ");
            switch (transaction.toLowerCase()) {
                case "доход":
                    typeTransaction = TypeTransaction.PROFIT;
                    correctInputTypeTransaction = true;
                    break;
                case "расход":
                    typeTransaction = TypeTransaction.EXPENSE;
                    correctInputTypeTransaction = true;
                    break;
                default:
                    userOutput.print("Тип транзакции не распознан, введите повторно.");
            }
        }
        return typeTransaction;
    }

    private void viewTransactions() {
        userOutput.print("Список транзакций:");
        userOutput.printTransactions(financeService.getTransactions(UserContext.getCurrentUser().email()));
    }

    private void filterTransactions() {
        Instant startDate = getDateFromUser("Введите начальную дату (в формате YYYY-MM-DD или оставьте пустым для всех): ");
        Instant endDate = getDateFromUser("Введите конечную дату (в формате YYYY-MM-DD или оставьте пустым для всех): ");
        String filterCategory = userInput.readString("Введите категорию (или оставьте пустым для всех): ");
        TypeTransaction filterType = getTypeTransactionFromUserInput();

        List<TransactionDto> filteredTransactions = financeService.filterTransactions(startDate, endDate, filterCategory, filterType, UserContext.getCurrentUser().email());
        userOutput.print("Отфильтрованные транзакции:");
        userOutput.printTransactions(filteredTransactions);
    }

    private Instant getDateFromUser(String prompt) {
        boolean correctData = false;
        Instant date = null;
        while (!correctData) {
            String dateInput = userInput.readString(prompt);
            if (dateInput.isEmpty()) {
                return null;
            }
            try {
                date = Instant.parse(dateInput + (prompt.contains("начальную") ? "T00:00:00Z" : "T23:59:59Z"));
                correctData = true;
            } catch (DateTimeParseException e) {
                userOutput.print("Не верный формат даты. Введите повторно или оставьте пустым для всех.");
            }
        }
        return date;
    }

    private TypeTransaction getTypeTransactionFromUserInput() {
        String filterTypeInput = userInput.readString("Введите тип транзакции (доход/расход, или оставьте пустым для всех): ");

        return switch (filterTypeInput.toLowerCase()) {
            case "доход" -> TypeTransaction.PROFIT;
            case "расход" -> TypeTransaction.EXPENSE;
            default -> null;
        };
    }


    private void editTransaction() {
        long id = userInput.readLong("Введите id транзакции для редактирования: ");
        double newAmount = userInput.readDouble("Введите новую сумму транзакции: ");
        String newCategory = userInput.readString("Введите новую категорию: ");
        String newDescription = userInput.readString("Введите новое описание: ");
        TypeTransaction newTypeTransaction = getTypeTransactionFromUser();

        if (financeService.editTransaction(new UpdateTransactionDto(id, newAmount, newCategory, Instant.now(), newDescription, newTypeTransaction)) != null) {
            userOutput.print("Транзакция успешно обновлена!");
        } else {
            userOutput.print("Не удалось обновить транзакцию. Проверьте индекс.");
        }
    }

    private void deleteTransaction() {
        long id = userInput.readLong("Введите id транзакции для удаления: ");
        if (financeService.removeTransactionUser(id)) {
            userOutput.print("Транзакция успешно удалена!");
        } else {
            userOutput.print("Не удалось удалить транзакцию. Проверьте индекс.");
        }
    }

    private void setSavingsGoal() {
        double savingGoal = userInput.readDouble("Введите цель накопления: ");
        targetService.updateGoalSavings(savingGoal);
        userOutput.print("Цель накопления установлена!");
    }

    private void trackProgress() {
        double progress = financeService.getProgressTowardsGoal(UserContext.getCurrentUser().email());
        userOutput.print("Прогресс накопления: " + progress);
    }

    private void setMonthlyBudget() {
        double monthlyBudget = userInput.readDouble("Введите месячный бюджет: ");
        targetService.setMonthlyBudget(monthlyBudget);
        userOutput.print("Месячный бюджет установлен!");
    }

    private void generateFinancialReport() {
        String report = targetService.generateFinancialReport();
        userOutput.print("Финансовый отчет:\n" + report);
    }

    private void blockUser() {
        if (isAdmin(UserContext.getCurrentUser())) {
            String emailToBlock = userInput.readString("Введите email пользователя для блокировки: ");
            if (userService.blockUser(emailToBlock)) {
                if(UserContext.getCurrentUser().email().equals(emailToBlock))
                    handleLoginLogout();
                userOutput.print("Пользователь успешно заблокирован!");
            } else {
                userOutput.print("Не удалось заблокировать пользователя. Проверьте email.");
            }
        } else {
            userOutput.print("У вас нет прав для блокировки пользователей.");
        }
    }

    private void deleteUser() {
        if (isAdmin(UserContext.getCurrentUser())) {
            String emailToDelete = userInput.readString("Введите email пользователя для удаления: ");
            if (userService.remove(emailToDelete)) {
                userOutput.print("Пользователь успешно удален!");
            } else {
                userOutput.print("Не удалось удалить пользователя. Проверьте email.");
            }
        } else {
            userOutput.print("У вас нет прав для удаления пользователей.");
        }
    }

    private void assignUserRole() {
        if (isAdmin(UserContext.getCurrentUser())) {
            String emailToAssign = userInput.readString("Введите email пользователя для назначения роли: ");
            String role = userInput.readString("Введите роль (ADMIN/USER): ");
            if (userService.changeUserRole(emailToAssign, role.equalsIgnoreCase("ADMIN")?Role.Admin:Role.User)) {
                userOutput.print("Роль успешно назначена!");
            } else {
                userOutput.print("Не удалось назначить роль. Проверьте email и роль.");
            }
        } else {
            userOutput.print("У вас нет прав для назначения ролей пользователям.");
        }
    }

    private boolean isAdmin(UserDto user) {
        return user.role().equals(Role.Admin);
    }
}

