package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ExchangeContext {

    private final List<TopicExchange> topics;


    public TopicExchange getExchange(IMessagingDomain domain) {
        return topics.stream()
                .filter(t -> t.getName().equals(domain.getExchangeName()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Топик не нашелся в списке"));
    }

    public void registerExchange(TopicExchange topicExchange) {
        topics.add(topicExchange);
    }

    public Set<TopicExchange> getAllExchanges() {
        return new HashSet<>(topics);
    }
}
