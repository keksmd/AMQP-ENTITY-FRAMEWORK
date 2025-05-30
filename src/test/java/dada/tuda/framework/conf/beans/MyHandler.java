package dada.tuda.framework.conf.beans;

import dada.tuda.framework.handling.MessageHandler;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@EqualsAndHashCode
public class MyHandler implements MessageHandler {
    private final String name = UUID.randomUUID().toString();

    @Override
    public Boolean canHandle(IMessagingDomain domain, IEventAction action) {
        return true;
    }

    @Override
    public Object handle(NormalizedMessage message) throws Exception {
        if (message.getObjectId() == null) {
            throw new IllegalStateException("object id is null");
        }
        return null;
    }
}
