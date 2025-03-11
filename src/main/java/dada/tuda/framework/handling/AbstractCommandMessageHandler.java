package dada.tuda.framework.handling;

import dada.tuda.framework.ex.EventHandlingException;
import dada.tuda.framework.normalization.AbstractNormalMessage;

public abstract class AbstractCommandMessageHandler implements MessageHandler {

    @Override
    public Object handle(AbstractNormalMessage message)throws Exception {
        handleCommand(message);
        return null;
    }

    public abstract void handleCommand(AbstractNormalMessage message) throws Exception ;
}
