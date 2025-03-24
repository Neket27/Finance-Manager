package app.aspect.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Set;

@Aspect
public class ValidationAspect {

    private final Validator validator;

    public ValidationAspect() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Pointcut("@annotation(app.validation.ValidateDto)")
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
