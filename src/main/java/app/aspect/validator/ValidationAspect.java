package app.aspect.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Aspect
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ValidationAspect {

    private final Validator validator;


    @Pointcut("@annotation(app.aspect.validator.ValidateDto)")
    public void validateDtoMethods() {
    }

    @Around("validateDtoMethods()")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        var method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
        var parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(ValidateDto.class)) {
                Object arg = args[i];
                Set<ConstraintViolation<Object>> violations = validator.validate(arg);
                if (!violations.isEmpty()) {
                    throw new IllegalArgumentException("Validation failed: " +
                            violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                                    .orElse("Ошибка валидации")
                    );
                }
            }
        }
        return joinPoint.proceed();
    }
}
