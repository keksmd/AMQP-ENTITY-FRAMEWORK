package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.DadaTudaFrameworkProperties;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class PerServiceQueueStrategy implements QueueStrategy {
    private final DadaTudaFrameworkProperties properties;
    @Value("${spring.application.name:}")
    private String applicationName;

    public String createQueueNameForDomain(IMessagingDomain domain) {
        String prefix = "";
        if (properties.getDomains().getQueue().getNaming().isPerService() || applicationName == null || applicationName.isEmpty()) {
            prefix = applicationName == null || applicationName.isEmpty()
                    ? "" : (applicationName + "-");
        }
        return prefix + domain.getKey() + "-queue";
    }

}
