package app.aspect.loggable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggableAspect {

    @Around("@within(app.aspect.loggable.Loggable) || @annotation(app.aspect.loggable.Loggable)")
    public Object loggableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Calling method: " + joinPoint.getSignature());
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (endTime - startTime));
        return result;
    }
}
