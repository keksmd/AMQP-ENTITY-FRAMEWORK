package dada.tuda.framework.crud;

import dada.tuda.framework.crud.contexts.BindingContext;
import dada.tuda.framework.crud.contexts.ExchangeContext;
import dada.tuda.framework.crud.contexts.QueueContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Objects;


@RequiredArgsConstructor
public class DynamicDeclarableRegistrar implements SmartInitializingSingleton {
    private final GenericApplicationContext ctx;
    private final RabbitAdmin admin;
    private final BindingContext bindingContext;
    private final ExchangeContext exchangeContext;
    private final QueueContext queueContext;

    @Override
    public void afterSingletonsInstantiated() {
        queueContext.getAllQueues().stream().filter(Objects::nonNull).forEach(d -> ctx.registerBean(d.getName(), Queue.class, () -> d));
        exchangeContext.getAllExchanges().stream().filter(Objects::nonNull).forEach(d -> ctx.registerBean(d.getName(), Exchange.class, () -> d));
        bindingContext.getBindings().stream().filter(Objects::nonNull).forEach(d -> ctx.registerBean(nameForBinding(d), Binding.class, () -> d));

        admin.initialize();
    }

    private String nameForBinding(Binding b) {
        return Binding.class + b.getExchange() + b.getRoutingKey();
    }
}