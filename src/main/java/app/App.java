package app;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.dto.user.CreateUserDto;
import app.entity.Role;
import app.entity.Transaction;
import app.entity.TypeTransaction;
import app.entity.User;
import app.mapper.UserMapper;
import app.repository.UserTableInMemory;
import app.service.AuthService;
import app.service.UserService;
import app.service.impl.AuthServiceImpl;
import app.service.impl.FinanceManagerServiceImpl;
import app.service.impl.UserServiceImpl;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserMapper userMapper = new UserMapper();
        AuthenticationConfig authenticationConfig = new AuthenticationConfig(new HashMap<>());
        Authenticator authenticator = new Authenticator(authenticationConfig);
        UserService userService = new UserServiceImpl(new UserMapper(), new UserTableInMemory());
        AuthService authService = new AuthServiceImpl(authenticationConfig, authenticator, userMapper, userService);
        FinanceManagerServiceImpl financeManager = new FinanceManagerServiceImpl(userService);


        System.out.println("Регистрация нового пользователя:");
        System.out.print("Имя: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        if (authService.register(new CreateUserDto(name, email, password))) {
            System.out.println("Регистрация успешна!");
        } else {
            System.out.println("Email уже занят.");
        }

        System.out.println("Авторизация:");
        System.out.print("Email: ");
        String loginEmail = scanner.nextLine();
        System.out.print("Пароль: ");
        String loginPassword = scanner.nextLine();

        User user = userService.getUserByEmail(loginEmail);

        if (authService.login(loginEmail, loginPassword)) {
            if (!user.isActive()) {
                System.out.println("Добро пожаловать, ВЫ заблокированы! И до свидания.");
            } else {
                System.out.println("Добро пожаловать, " + user.getName() + "!");

                boolean running = true;
                while (running) {
                    System.out.println("\nВыберите действие:");
                    System.out.println("1. Добавить транзакцию");
                    System.out.println("2. Просмотреть транзакции");
                    System.out.println("3. Фильтровать транзакции");
                    System.out.println("4. Редактировать транзакцию");
                    System.out.println("5. Удалить транзакцию");
                    System.out.println("6. Установить цель накопления");
                    System.out.println("7. Отслеживать прогресс по цели накопления");
                    System.out.println("8. Установить месячный бюджет");
                    System.out.println("9. Сгенерировать финансовый отчет");
                    if (isAdmin(user)) {
                        System.out.println("10. Блокировка пользователя");
                        System.out.println("11. Удаление пользователя");
                    }
                    System.out.println("12. Выход");

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.print("Введите сумму транзакции: ");
                            double amount = scanner.nextDouble();
                            scanner.nextLine(); // Очистка буфера
                            System.out.print("Введите категорию: ");
                            String category = scanner.nextLine();
                            System.out.print("Введите описание: ");
                            String description = scanner.nextLine();
                            System.out.println("Введите тип транзакции (доход/расход): ");
                            boolean correctInputTypeTransaction = false;
                            TypeTransaction typeTransaction = null;
                            while (!correctInputTypeTransaction) {
                                String transaction = scanner.nextLine();
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
                                        System.out.println("Тип транзакции не распознан, введите повторно.");
                                        correctInputTypeTransaction = false;
                                }
                            }
                            financeManager.addTransaction(amount, category, Instant.now(), description, typeTransaction, email);
                            System.out.println("Транзакция добавлена!");
                            break;

                        case 2:
                            System.out.println("Список транзакций:");
                            for (Transaction transaction : user.getFinance().getTransactions()) {
                                System.out.println("Сумма: " + transaction.getAmount() +
                                        ", Категория: " + transaction.getCategory() +
                                        ", Описание: " + transaction.getDescription() +
                                        ", Тип: " + (transaction.getTypeTransaction().equals(TypeTransaction.PROFIT) ? "Доход" : "Расход"));
                            }
                            break;

                        case 3:
                            // Фильтрация транзакций
                            System.out.println("Введите начальную дату (в формате YYYY-MM-DD или оставьте пустым для всех): ");
                            boolean correctData = false;
                            Instant startDate = null;
                            while (!correctData) {
                                String startDateInput = scanner.nextLine();
                                if (startDateInput.isEmpty())
                                    break;

                                try {
                                    startDate = Instant.parse(startDateInput + "T00:00:00Z");
                                    correctData = true;
                                } catch (DateTimeParseException e) {
                                    System.out.println("Не верный формат даты. Введите повторно или оставьте пустым для всех.");
                                }
                            }

                            correctData = false;
                            Instant endDate = null;
                            System.out.println("Введите конечную дату (в формате YYYY-MM-DD или оставьте пустым для всех): ");
                            while (!correctData) {
                                try {
                                    String endDateInput = scanner.nextLine();
                                    if (endDateInput.isEmpty())
                                        break;

                                    endDate = Instant.parse(endDateInput + "T23:59:59Z");
                                    correctData = true;
                                } catch (DateTimeParseException e) {
                                    System.out.println("Не верный формат даты. Введите повторно или оставьте пустым для всех.");
                                }
                            }

                            System.out.println("Введите категорию (или оставьте пустым для всех): ");
                            String filterCategory = scanner.nextLine();

                            System.out.println("Введите тип транзакции (доход/расход, или оставьте пустым для всех): ");
                            String filterTypeInput = scanner.nextLine();
                            TypeTransaction filterType = null;
                            if (filterTypeInput.equalsIgnoreCase("доход")) {
                                filterType = TypeTransaction.PROFIT;
                            } else if (filterTypeInput.equalsIgnoreCase("расход")) {
                                filterType = TypeTransaction.EXPENSE;
                            }

                            List<Transaction> filteredTransactions = financeManager.filterTransactions(startDate, endDate, filterCategory, filterType, email);
                            System.out.println("Отфильтрованные транзакции:");
                            for (Transaction transaction : filteredTransactions) {
                                System.out.println("Сумма: " + transaction.getAmount() +
                                        ", Категория: " + transaction.getCategory() +
                                        ", Дата: " + transaction.getDate() +
                                        ", Описание: " + transaction.getDescription() +
                                        ", Тип: " + (transaction.getTypeTransaction().equals(TypeTransaction.PROFIT) ? "Доход" : "Расход"));
                            }
                            break;

                        case 4:
                            System.out.print("Введите индекс транзакции для редактирования: ");
                            int indexToEdit = scanner.nextInt();
                            scanner.nextLine(); // Очистка буфера
                            System.out.print("Введите новую сумму: ");
                            double newAmount = scanner.nextDouble();
                            scanner.nextLine(); // Очистка буфера
                            System.out.print("Введите новую категорию: ");
                            String newCategory = scanner.nextLine();
                            System.out.print("Введите новое описание: ");
                            String newDescription = scanner.nextLine();
                            financeManager.editTransaction(indexToEdit, newAmount, newCategory, newDescription);
                            System.out.println("Транзакция обновлена!");
                            break;

                        case 5:
                            System.out.print("Введите индекс транзакции для удаления: ");
                            int indexToDelete = scanner.nextInt();
                            scanner.nextLine(); // Очистка буфера
                            financeManager.deleteTransaction(indexToDelete);
                            System.out.println("Транзакция удалена!");
                            break;

                        case 6:
                            System.out.print("Введите цель накопления: ");
                            double goal = scanner.nextDouble();
                            financeManager.setSavingsGoal(goal);
                            break;

                        case 7:
                            double progress = financeManager.getProgressTowardsGoal(email);
                            System.out.println("Вы достигли " + progress + "% от вашей цели накопления.");
                            break;

                        case 8:
                            System.out.print("Введите месячный бюджет: ");
                            double budget = scanner.nextDouble();
                            financeManager.setMonthlyBudget(budget);
                            break;

                        case 9:
                            financeManager.generateFinancialReport(email);
                            System.out.println("Финансовый отчет сгенерирован!");
                            break;

                        case 10:
                            if (isAdmin(user)) {
                                System.out.println("Введите email пользователя");
                                {
                                    String mail = scanner.nextLine();
                                    userService.blockUser(mail);
                                }
                            }
                            break;

                        case 11:
                            if (isAdmin(user)) {
                                System.out.println("Введите email пользователя");
                                {
                                    String mail = scanner.nextLine();
                                    userService.remove(mail);
                                }
                            }

                        case 12:
                            // Выход
                            running = false;
                            System.out.println("Вы вышли из системы.");
                            break;

                        default:
                            System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
                            break;
                    }
                }
            }
        } else {
            System.out.println("Неверный email или пароль.");
        }

        scanner.close();
    }

    private static boolean isAdmin(User user) {
        return user.getRole().equals(Role.Admin);
    }
}

