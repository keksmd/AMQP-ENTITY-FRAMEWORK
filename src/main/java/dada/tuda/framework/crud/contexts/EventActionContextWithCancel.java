package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.crud.extractor.RoutingKeyConverter;
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dada.tuda.framework.normalization.types.realizations.CancelPayload.CANCEL_DOMAIN;

public class EventActionContextWithCancel implements IEventActionContext, ICancelEventActionContext {
    private final Map<String, IEventAction> name2actionMap;
    private final QueueNameContext queueNameContext;
    private final ExchangeContext exchangeContext;
    private final DomainContext domainContext;
    private final RabbitAdmin rabbitAdmin;
    private final RoutingKeyConverter routingKeyConverter;
    private IMessagingDomain cancelDomain;

    public EventActionContextWithCancel(ConnectionFactory connectionFactory, List<IEventAction> values, QueueNameContext queueNameContext, DomainContext domainContext, ExchangeContext exchangeContext, RoutingKeyConverter routingKeyConverter) {
        this.queueNameContext = queueNameContext;
        this.exchangeContext = exchangeContext;
        this.domainContext = domainContext;
        this.routingKeyConverter = routingKeyConverter;
        name2actionMap = new ConcurrentHashMap<>();
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        for (IEventAction action : values) {
            name2actionMap.put(action.name(), action);
        }
    }


    @Override
    public IEventAction getByName(String name) {
        if (CancelEventActionTemplate.nameIsCancel(name)) {
            String originalName = CancelEventActionTemplate.getOriginalNameFromCancel(name);
            return getOrCreateCancelByAction(name2actionMap.get(originalName));
        } else {
            return name2actionMap.get(name);
        }
    }

    @Override
    public CancelEventActionTemplate getOrCreateCancelByAction(IEventAction action) {
        String cancelName = CancelEventActionTemplate.createNameForCancelByOriginal(action.name());
        var act = name2actionMap.computeIfAbsent(cancelName, name -> {
            var cancel = new CancelEventActionTemplate(action);
            addLazyCancelBinding(cancel);
            return cancel;
        });
        if (act instanceof CancelEventActionTemplate cancelEventActionTemplate) {
            return cancelEventActionTemplate;
        } else {
            throw new IllegalStateException("Action with name " + cancelName + " is not a CancelEventActionTemplate");
        }
    }

    private void addLazyCancelBinding(IEventAction cancel) {

        var cancelExchange = exchangeContext.getExchange(cancelDomain);
        for (String qName : queueNameContext.getQueueNameListByDomain(cancelDomain)) {
            Queue queue = new Queue(qName, true);
            Binding binding = BindingBuilder.bind(queue).to(cancelExchange).with(routingKeyConverter.toRoutingKey(cancelDomain, cancel));
            rabbitAdmin.declareBinding(binding);
        }
    }

    public void init() {
        if (cancelDomain == null) {
            this.cancelDomain = domainContext.getByName(CANCEL_DOMAIN);
        }
    }

    @Override
    public List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain) {
        if (cancelDomain.equals(domain)) {
            return name2actionMap.values().stream().filter(CancelEventActionTemplate.class::isInstance).toList();
        } else {
            return name2actionMap.values().stream().filter(action -> !(action instanceof CancelEventActionTemplate)).toList();
        }
    }
}
