//package app.util.in;
//
//import app.context.UserContext;
//import app.dto.user.CreateUserDto;
//import app.service.AuthService;
//import app.service.UserService;
//import app.util.out.UserOutput;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class UserAuth {
//
//    private final Logger log = LoggerFactory.getLogger(UserAuth.class);
//    private final AuthService authService;
//    private final UserOutput userOutput;
//    private final UserInput userInput;
//
//    public UserAuth(UserService userService, AuthService authService, UserOutput userOutput, UserInput userInput) {
//        this.authService = authService;
//        this.userOutput = userOutput;
//        this.userInput = userInput;
//    }
//
//    public void registerUser() {
//        System.out.println("Регистрация нового пользователя:");
//
//        String name = userInput.readString("Имя: ");
//        String email = userInput.readString("Email: ");
//        String password = userInput.readString("Пароль: ");
//
//         authService.register(new CreateUserDto(name, email, password));
//
//
//
//    }
//
//    public void loginUser() {
//        userOutput.print("Авторизация:");
//        String loginEmail = userInput.readString("Email: ");
//        String loginPassword = userInput.readString("Пароль: ");
//
//        if (!authService.login(loginEmail, loginPassword))
//            userOutput.print("Неверный email или пароль.");
//        else
//            userOutput.print("Добро пожаловать, " + UserContext.getCurrentUser().name());
//
//    }
//
//    public void logoutUser() {
//        if (authService.logout())
//            userOutput.print("Вы вышли из аккаунта.");
//        else
//            log.warn("The user with id: {} was unable to log out of his account.", UserContext.getCurrentUser().email());
//    }
//}
