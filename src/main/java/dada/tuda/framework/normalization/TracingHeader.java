package dada.tuda.framework.normalization;

import org.slf4j.MDC;

import java.util.Map;

public class TracingHeader implements Header {
    @Override
    public void accept(Map<String, Object> stringObjectMap) {
        String requestId = MDC.get("requestId");
        if (requestId != null) {
            stringObjectMap.put("requestId", requestId);
        }
    }
}
