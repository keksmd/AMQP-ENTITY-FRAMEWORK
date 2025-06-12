package dada.tuda.framework.handling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.InvocationResult;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CancelableMessageHandlerAdapter extends MessageListenerAdapter implements Cancelable, MessageHandler {
    private final ObjectMapper objectMapper;
    private final Method delegateMethod;
    private Method cancelDelegateMethod;

    public CancelableMessageHandlerAdapter(ObjectMapper objectMapper, Method delegateMethod, Object delegateObject) {
        super(delegateObject, delegateMethod.getName());
        this.objectMapper = objectMapper;
        this.delegateMethod = delegateMethod;
    }

    public CancelableMessageHandlerAdapter(ObjectMapper objectMapper, Method delegateMethod, Method cancelDelegateMethod, Object delegateObject) {
        super(delegateObject, delegateMethod.getName());
        this.objectMapper = objectMapper;
        this.delegateMethod = delegateMethod;
        this.cancelDelegateMethod = cancelDelegateMethod;
    }

    @Override
    public void cancel(NormalizedMessage message) throws Exception {
        if (cancelDelegateMethod != null && message.getActionType() != null && message.getActionType().isCancelable()) {
            Object[] listenerArguments = buildListenerArguments(message, null, null);
            invokeListenerMethod(cancelDelegateMethod.getName(), listenerArguments, null);
        }
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
                    Object payload = objectMapper.convertValue(msg.getPayloadMap(), payloadType);
                    return new Object[]{ payload };
                } else if (params.length == 2) {
                    if (NormalMessage.class.isAssignableFrom(params[0].getType())) {
                        Class<?> payloadType = params[1].getType();
                        Object payload = objectMapper.convertValue(msg.getPayloadMap(), payloadType);
                        return new Object[]{ msg, payload };
                    } else if (NormalMessage.class.isAssignableFrom(params[1].getType())) {
                        Class<?> payloadType = params[0].getType();
                        Object payload = objectMapper.convertValue(msg.getPayloadMap(), payloadType);
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
    public Object handle(NormalizedMessage message, Message rawMessage) throws Exception {
        Object[] listenerArguments = buildListenerArguments(message, null, null);
        Object result = invokeListenerMethod(delegateMethod.getName(), listenerArguments, null);
        if (result != null && message.getActionType() != null && message.getActionType().isQuery()) {
            handleResult(new InvocationResult(result, null, null, null, null), rawMessage, null);
        } else {
            logger.trace("No result object given - no result to handle");
        }
        return result;
    }


}
