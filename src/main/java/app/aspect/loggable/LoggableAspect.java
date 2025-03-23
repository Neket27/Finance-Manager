package app.aspect.loggable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggableAspect {

    @Pointcut("within(@app.aspect.loggable *) && execution(* * (..))")
    public void annotateByLoggable() {
    }

    @Around("annotateByLoggable()")
    public Object loggableAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Calling method: " + joinPoint.getSignature());
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (endTime - startTime));
        return result;
    }
}
