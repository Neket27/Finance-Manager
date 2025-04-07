package app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable;

import neket27.context.UserContext;
import neket27.entity.UserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class AuditAspect {

    private final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Before("@annotation(auditable)")
    public void audit(JoinPoint joinPoint, Auditable auditable) {
        String action = auditable.action();
        String user = getCurrentUserFromThreadLocal();
        Object[] args = joinPoint.getArgs();

        log.info("User: " + user + ", Action: " + action + ", Args: " + Arrays.toString(args));
    }


    private String getCurrentUserFromThreadLocal() {

        return UserContext.getCurrentUser() != null ? ((UserDetails) UserContext.getCurrentUser()).getEmail() : "Anonymous";
    }

}
