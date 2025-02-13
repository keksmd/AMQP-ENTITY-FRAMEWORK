package site.podpivasniki.mytemplatestarter.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import site.podpivasniki.mytemplatestarter.logging.IntegrationLog.Direction;

@Aspect
public class IntegrationLogAspect {

  private static final Logger log = LoggerFactory.getLogger("IntegrationLogger");
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private void createContext(ProceedingJoinPoint joinPoint, IntegrationLog integrationLog) {
    if (integrationLog.direction() == Direction.IN) {
      MDC.clear();
    }
    try {
      String requestJson = makeInput(joinPoint);
      MDC.put("rq", requestJson);
    } catch (JsonProcessingException e) {
      MDC.put("rq", "Ошибка сериализации входных параметров");
    }finally {
        MDC.put("direction", integrationLog.direction().toString());
        MDC.put("eventType", integrationLog.eventType());
    }
  }

  private static String makeInput(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    Map<String, Object> paramMap = new HashMap<>();
    for (int i = 0; i < parameterNames.length; i++) {
      paramMap.put(parameterNames[i], args[i]);
    }

    String requestJson = objectMapper.writeValueAsString(paramMap);
    return requestJson;
  }

  @Pointcut("@annotation(integrationLog)")
  public void isLoggable(IntegrationLog integrationLog) {
  }

  @Around(value = "isLoggable(integrationLog)", argNames = "joinPoint, integrationLog")
  public Object logMethodExecution(ProceedingJoinPoint joinPoint, IntegrationLog integrationLog)
      throws Throwable {
    createContext(joinPoint, integrationLog);

    Object result = null;
    try {
      result = joinPoint.proceed();
      String responseJson = objectMapper.writeValueAsString(result);
      MDC.put("rs", responseJson);
      log.info("success");
    } catch (Throwable ex) {
      MDC.put("error", ex.getMessage());
      log.error("failed");
      throw ex;
    } finally {
      MDC.clear();
    }

    return result;
  }
}
