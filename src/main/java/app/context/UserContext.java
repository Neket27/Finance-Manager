package app.context;

import app.container.Component;
import app.dto.user.UserDto;

@Component
public class UserContext {
    private static final ThreadLocal<UserDto> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(UserDto user) {
        currentUser.set(user);
    }

    public static UserDto getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
