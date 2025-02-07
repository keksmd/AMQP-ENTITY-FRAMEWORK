package site.podpivasniki.mytemplatestarter.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
      String requestJson = objectMapper.writeValueAsString(joinPoint.getArgs());
      MDC.put("rq", requestJson);
      MDC.put("eventType", integrationLog.eventType());
    } catch (JsonProcessingException e) {
      MDC.put("rq", "Ошибка сериализации входных параметров");
    }
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
    } catch (Throwable ex) {
      MDC.put("error", ex.getMessage());
      throw ex;
    } finally {
      log.info("{}", getMdcAsJson());
      MDC.clear();
    }

    return result;
  }

  private String getMdcAsJson() {
    try {
      return objectMapper.writeValueAsString(MDC.getCopyOfContextMap());
    } catch (JsonProcessingException e) {
      return "{\"error\": \"Ошибка сериализации MDC\"}";
    }
  }
}
