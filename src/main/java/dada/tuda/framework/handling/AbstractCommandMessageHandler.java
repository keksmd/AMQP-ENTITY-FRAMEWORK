package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.messages.NormalizedMessage;

public abstract class AbstractCommandMessageHandler implements MessageHandler {

    @Override
    public Object handle(NormalizedMessage message) throws Exception {
        handleCommand(message);
        return null;
    }

    public abstract void handleCommand(NormalMessage message) throws Exception;
}
