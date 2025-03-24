package app.aspect.exception;

import app.handler.GlobalExceptionHandlers;
import app.handler.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.Instant;

@Aspect
public class CustomExceptionAspect {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final GlobalExceptionHandlers handler = new GlobalExceptionHandlers();

    @Around("@within(app.aspect.exception.CustomExceptionHandler) || @annotation(app.aspect.exception.CustomExceptionHandler)")
    public Object handleServletExceptions(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            HttpServletResponse response = extractHttpServletResponse(pjp.getArgs());
            if (response != null) {
                Response customResponse = resolveHandler(ex);
                int status = customResponse.status();
                response.setStatus(status);
                response.setContentType("application/json");
                PrintWriter writer = response.getWriter();
                writer.write(mapper.writeValueAsString(customResponse));
                writer.flush();
            }
            return null;
        }
    }

    private HttpServletResponse extractHttpServletResponse(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof HttpServletResponse) {
                return (HttpServletResponse) arg;
            }
        }
        return null;
    }

    private Response resolveHandler(Exception ex) throws Exception {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                ExceptionHandler exceptionHandler = method.getAnnotation(ExceptionHandler.class);
                if (exceptionHandler.value().isAssignableFrom(ex.getClass())) {
                    ResponseStatus status = method.getAnnotation(ResponseStatus.class);
                    int httpStatus = status != null ? status.value() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

                    Response r = (Response) method.invoke(handler, ex);
                    return new Response(httpStatus, r.error(),Instant.now());
                }
            }
        }

        return new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error", Instant.now());
    }

}
