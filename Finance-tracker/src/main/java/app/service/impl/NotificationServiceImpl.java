package app.service.impl;

import app.service.NotificationService;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
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
