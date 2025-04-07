package neket27.context;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private static final ThreadLocal<Object> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(Object user) {
        currentUser.set(user);
    }

    public static Object getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }


    public void setUser(Object user) {
        currentUser.set(user);
    }

    public static Object getUser() {
        return currentUser.get();
    }

}
