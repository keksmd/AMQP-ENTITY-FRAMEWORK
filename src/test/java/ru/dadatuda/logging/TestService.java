package dada.tuda.framework.logging;

import dada.tuda.framework.logging.IntegrationLog.Direction;
import org.springframework.stereotype.Service;

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
