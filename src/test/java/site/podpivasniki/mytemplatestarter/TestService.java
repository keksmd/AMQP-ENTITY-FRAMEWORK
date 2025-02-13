package site.podpivasniki.mytemplatestarter;

import org.springframework.stereotype.Service;
import site.podpivasniki.mytemplatestarter.logging.IntegrationLog;
import site.podpivasniki.mytemplatestarter.logging.IntegrationLog.Direction;

@Service
public class TestService {

  @IntegrationLog(direction = Direction.IN, eventType = "TestEventType")
  public String testMethod(String input) {
    return "Processed: " + input;
  }

  @IntegrationLog(direction = Direction.IN, eventType = "TestEventType")
  public String testMethodThrowEx(String input) {
    throw new RuntimeException("Ex");
  }
}
