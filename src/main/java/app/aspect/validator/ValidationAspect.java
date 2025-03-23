package app.aspect.validator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Set;

@Aspect
public class ValidationAspect {

    private final Validator validator;

    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }

    @Around("execution(* jakarta.servlet.http.HttpServlet.doPost(..)) && args(.., req, res)")
    public Object validate(ProceedingJoinPoint joinPoint, HttpServletRequest req, HttpServletResponse res) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg != null && arg.getClass().isAnnotationPresent(ValidateDto.class)) {
                Set<ConstraintViolation<Object>> violations = validator.validate(arg);
                if (!violations.isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    String errors = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                            .orElse("Ошибка валидации");
                    res.getWriter().write("{\"errors\":\"" + errors + "\"}");
                    return null;
                }
            }
        }
        return joinPoint.proceed();
    }
}
