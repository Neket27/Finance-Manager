package app.service.impl;

import app.aspect.loggable.Loggable;
import app.service.NotificationService;

@Loggable
public class NotificationServiceImpl implements NotificationService {


    @Override
    public void sendMessage(String email, String... messages) {
        // TODO логика для отправки email-уведомления
    }

}
