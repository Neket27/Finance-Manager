package app.service.impl;

import app.container.Component;
import app.service.NotificationService;

@Component
public class NotificationServiceImpl implements NotificationService {


    @Override
    public void sendMessage(String email, String... messages) {
        // TODO логика для отправки email-уведомления
    }

}
