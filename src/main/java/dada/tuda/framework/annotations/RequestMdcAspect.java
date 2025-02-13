package dada.tuda.framework.annotations;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Aspect
public class RequestMdcAspect {


    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public Object handleMdc(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            try {
                String requestId;
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    requestId = request.getHeader("X-Request-Id");
                    if (requestId == null) {
                        requestId = UUID.randomUUID().toString(); // Генерируем UUID, если заголовок отсутствует
                    }
                    requestId = requestId + ":" + request.getRequestURI();
                } else {
                    requestId = UUID.randomUUID().toString();
                }

                MDC.put("requestId", requestId);
            } catch (Exception e) {
                log.error("Не удалось установить requestId. Подробнее: {}", e.getLocalizedMessage());
            }
            return joinPoint.proceed();
        } finally {
            MDC.remove("requestId");

        }
    }
}
