package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggableAspect {

    private final Logger log = LoggerFactory.getLogger(LoggableAspect.class);

    @Around("(@within(neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging) || @annotation(neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging)) && execution(public * *(..))")
    public Object loggableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Calling method: " + joinPoint.getSignature());
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();

            log.debug("Method: {}, time taken: {}", joinPoint.getSignature().getName(), (endTime - startTime));
            return result;

        } catch (Throwable throwable) {
            log.error("Ошибка при выполнении метода {}: {}", joinPoint.getSignature().getName(), throwable.getMessage(), throwable);
            throw throwable;
        }

    }

}
