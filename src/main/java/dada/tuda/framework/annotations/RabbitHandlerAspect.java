package dada.tuda.framework.annotations;

import dada.tuda.framework.normalization.AbstractNormalMessage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

@Aspect
@Slf4j
public class RabbitHandlerAspect {
    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitHandler)")
    public Object handleRabbitMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            try {
                // Извлекаем аргументы метода
                Object[] args = joinPoint.getArgs();
                String requestId = null;
                if (args.length > 1 && args[1] instanceof String headerValue) {
                    requestId = headerValue;
                } else {
                    for (Object arg : args) {
                        if (arg instanceof String headerValue) {
                            requestId = headerValue;
                            break;
                        }
                    }
                }
                String type = null;
                if (args.length > 0 && args[0] instanceof AbstractNormalMessage bodyValue) {
                    if (requestId == null) {
                        requestId = bodyValue.getOperationId();
                    }
                    type = bodyValue.getType().name();
                } else {
                    for (Object arg : args) {
                        if (arg instanceof AbstractNormalMessage bodyValue) {
                            type = bodyValue.getType().name();
                            break;
                        }
                    }
                }
                if (requestId == null) {
                    requestId = java.util.UUID.randomUUID().toString();
                }
                requestId += ":" + (type != null ? type : "unknownEvent");
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
