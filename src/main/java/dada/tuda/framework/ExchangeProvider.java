package dada.tuda.framework;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ExchangeProvider {

    private final List<TopicExchange> topics;
    private final Map<IMessagingDomain, TopicExchange> exchangeMap = new HashMap<>();
    private final ApplicationContext applicationContext;


    public TopicExchange getExchange(IMessagingDomain aggregate) {
        return topics.stream().filter(t -> t.getName().equals(aggregate.getExchangeName())).findFirst().orElseThrow(() -> new IllegalArgumentException("Топик не нашелся в списке"));
    }
}
