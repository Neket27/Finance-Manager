package app.service.impl;

import app.aspect.loggable.CustomLogging;
import app.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
@CustomLogging
public class NotificationServiceImpl implements NotificationService {


    @Override
    public void sendMessage(String email, String... messages) {
        // TODO логика для отправки email-уведомления
    }

}
