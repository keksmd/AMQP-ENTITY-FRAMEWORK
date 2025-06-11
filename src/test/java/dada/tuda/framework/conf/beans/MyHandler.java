package dada.tuda.framework.conf.beans;

import dada.tuda.framework.handling.MessageHandler;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@EqualsAndHashCode
public class MyHandler implements MessageHandler {
    private final String name = UUID.randomUUID().toString();

    @Override
    public boolean canHandle(NormalizedMessage message) {
        return true;
    }

    @Override
    public Object handle(NormalizedMessage message, Object payload) throws Exception {
        if (message.getObjectId() == null) {
            throw new IllegalStateException("object id is null");
        }
        return null;
    }
}
