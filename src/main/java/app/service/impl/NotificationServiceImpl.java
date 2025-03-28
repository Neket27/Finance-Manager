package app.service.impl;

import app.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {


    @Override
    public void sendMessage(String email, String... messages) {
        // TODO логика для отправки email-уведомления
    }

}
