package dada.tuda.framework.handling;

import com.rabbitmq.client.Channel;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.handling.conversion.RabbitHandlerArgumentResolverComposite;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class CancelableMessageHandlerAdapter extends MessageListenerAdapter implements Cancelable, MessageHandler {
    private final Method delegateMethod;
    private final IEventActionContext eventActionContext;
    private final RabbitHandlerArgumentResolverComposite argumentResolverComposite;

    public CancelableMessageHandlerAdapter(Method delegateMethod, Object delegateObject, IEventActionContext eventActionContext, RabbitHandlerArgumentResolverComposite argumentResolverComposite) {
        super(delegateObject, delegateMethod.getName());
        this.delegateMethod = delegateMethod;
        this.eventActionContext = eventActionContext;
        this.argumentResolverComposite = argumentResolverComposite;
    }

    @Override
    public Object handle(NormalMessage message) {
        Object[] listenerArguments = buildListenerArguments(message, null, null);
        Object result = invokeListenerMethod(delegateMethod.getName(), listenerArguments, null);
        IEventAction action = eventActionContext.getByName(message.getActionTypeName());
        if (result != null && action != null && action.isQuery()) {
            //handleResult(new InvocationResult(result, new LiteralExpression(rawMessage.getMessageProperties().getReplyTo()),result.getClass(), rawMessage.getMessageProperties().getTargetBean(), rawMessage.getMessageProperties().getTargetMethod()), rawMessage, channel);
            return result;
        } else {
            logger.trace("No result object given - no result to handle");
        }
        return null;
    }

    @Override
    protected Object[] buildListenerArguments(Object extractedMessage, Channel channel, Message message) {
        if (extractedMessage instanceof NormalMessage normalMessage) {
            return argumentResolverComposite.resolveArguments(delegateMethod, normalMessage);
        }
        return new Object[]{ extractedMessage };
    }

    @Override
    public void cancel(NormalMessage message) throws Exception {
        Object[] listenerArguments = null;
        try {
            if (delegateMethod != null && message.getActionTypeName() != null) {
                listenerArguments = buildListenerArguments(message, null, null);
                invokeListenerMethod(delegateMethod.getName(), listenerArguments, null);
            }
        } catch (Throwable e) {
            log.error("THIS WILL CAUSE DATA IMPERSISTENCE !!! Failed to cancel during distributed transaction message(id= {} , domain={},action={}) with listener(method={}, args={})", message.getOperationId(), message.getDomainName(), message.getActionTypeName(), delegateMethod.getName(), Arrays.toString(listenerArguments), e);
        }

    }
}