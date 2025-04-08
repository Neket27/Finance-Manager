package app.aspect.auditable;

import app.context.UserContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Aspect
public class AuditAspect {

    private final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Before("@annotation(auditable)")
    public void audit(JoinPoint joinPoint, Auditable auditable) {
        String action = auditable.action();
        String user = UserContext.getCurrentUser() != null ? UserContext.getCurrentUser().email() : "Anonymous";
        Object[] args = joinPoint.getArgs();

        log.info("User: " + user + ", Action: " + action + ", Args: " + Arrays.toString(args));
    }
}
