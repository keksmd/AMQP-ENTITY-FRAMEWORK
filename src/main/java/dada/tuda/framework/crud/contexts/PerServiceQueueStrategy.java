package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class PerServiceQueueStrategy implements QueueStrategy {
    @Value("${spring.application.name:}")
    private String applicationName;
    @Value("dada.tuda.framework.domains.queue.naming.per-service:true")
    private boolean isPerService;

    public String createQueueNameForDomain(IMessagingDomain domain) {
        String prefix = "";
        if (isPerService || applicationName == null || applicationName.isEmpty()) {
            prefix = applicationName == null || applicationName.isEmpty()
                    ? "" : (applicationName + "-");
        }
        return prefix + domain.getKey() + "-queue";
    }

}
