package app.aspect.loggable;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggableAspect {

    @Around("(@within(app.aspect.loggable.CustomLogging) || @annotation(app.aspect.loggable.CustomLogging)) && execution(public * *(..))")
    public Object loggableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Calling method: " + joinPoint.getSignature());
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();

            log.debug("Time taken: " + (endTime - startTime));
            return result;

        } catch (Throwable throwable) {
            log.error("Ошибка при выполнении метода {}: {}", joinPoint.getSignature().getName(), throwable.getMessage(), throwable);
            throw throwable;
        }

    }


}
