package site.podpivasniki.mytemplatestarter.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Arrays;

@Aspect
public class IntegrationLogAspect {

    private static final Logger log = LoggerFactory.getLogger("IntegrationLogger");

    @Around("@annotation(site.podpivasniki.mytemplatestarter.logging.IntegrationLog)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        Object[] args = joinPoint.getArgs();
        log.info("Вход в метод: {} с параметрами: {}", methodName, Arrays.toString(args));

        Object result = joinPoint.proceed();

        log.info("Выход из метода: {} с результатом: {}", methodName, result);
        return result;
    }
}
