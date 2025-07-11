package dada.tuda.framework.handling;

import com.rabbitmq.client.Channel;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.PayloadConverter;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CancelableMessageHandlerAdapter extends MessageListenerAdapter implements Cancelable, MessageHandler {
    private final Method delegateMethod;
    private final PayloadConverter payloadConverter;
    private final IEventActionContext eventActionContext;

    public CancelableMessageHandlerAdapter(Method delegateMethod, Object delegateObject, PayloadConverter payloadConverter, IEventActionContext eventActionContext) {
        super(delegateObject, delegateMethod.getName());
        this.delegateMethod = delegateMethod;
        this.payloadConverter = payloadConverter;
        this.eventActionContext = eventActionContext;
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
        if (extractedMessage instanceof NormalMessage msg) {
            try {
                Parameter[] params = delegateMethod.getParameters();

                if (params.length == 1) {
                    Class<?> payloadType = params[0].getType();
                    if (NormalMessage.class.isAssignableFrom(payloadType)) {
                        return new Object[]{ msg };
                    }
                    Object payload = payloadConverter.convertPayload(msg.getPayloadMap(), payloadType);
                    return new Object[]{ payload };
                } else if (params.length == 2) {
                    if (NormalMessage.class.isAssignableFrom(params[0].getType())) {
                        Class<?> payloadType = params[1].getType();
                        Object payload = payloadConverter.convertPayload(msg.getPayloadMap(), payloadType);
                        return new Object[]{ msg, payload };
                    } else if (NormalMessage.class.isAssignableFrom(params[1].getType())) {
                        Class<?> payloadType = params[0].getType();
                        Object payload = payloadConverter.convertPayload(msg.getPayloadMap(), payloadType);
                        return new Object[]{ msg, payload };
                    } else {
                        throw new IllegalArgumentException("Invalid parameter signature in method " + delegateMethod.getName());
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported handler method signature: " + delegateMethod.getName());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to build listener arguments", e);
            }
        } else return new Object[]{ extractedMessage };
    }


    @Override
    public void cancel(NormalMessage message) throws Exception {
        if (delegateMethod != null && message.getActionTypeName() != null) {
            Object[] listenerArguments = buildListenerArguments(message, null, null);
            invokeListenerMethod(delegateMethod.getName(), listenerArguments, null);
        }
    }


}
