package app.service;

public interface NotificationService {

    void sendMessage(String email, String... messages);
}
