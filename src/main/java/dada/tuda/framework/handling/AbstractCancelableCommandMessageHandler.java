package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.AbstractNormalMessage;

public abstract class AbstractCancelableCommandMessageHandler extends AbstractCommandMessageHandler implements CancelableMessageHandler {

    @Override
    public Object handle(AbstractNormalMessage message) throws Exception {
        handleCommand(message);
        return null;
    }

    public abstract void handleCommand(AbstractNormalMessage message) throws Exception ;
}
