package dada.tuda.framework.handling;

import dada.tuda.framework.ex.EventHandlingException;
import dada.tuda.framework.normalization.AbstractNormalMessage;

public abstract class AbstractCancelableCommandMessageHandler extends AbstractCommandMessageHandler implements CancelableMessageHandler {

    @Override
    public Object handle(AbstractNormalMessage message) throws EventHandlingException {
        handleCommand(message);
        return null;
    }

    public abstract void handleCommand(AbstractNormalMessage message) throws EventHandlingException;
}
