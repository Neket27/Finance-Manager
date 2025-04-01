package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
@CustomLogging
public class NotificationServiceImpl implements NotificationService {

    @Override
    @Auditable
    public void sendMessage(String email, String... messages) {
        // TODO логика для отправки email-уведомления
    }

}
