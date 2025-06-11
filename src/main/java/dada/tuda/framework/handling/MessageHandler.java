package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalizedMessage;
import org.springframework.amqp.core.Message;

public interface MessageHandler {
    boolean canHandle(NormalizedMessage message);

    void handle(NormalizedMessage message, Message raw) throws Exception;
}
