package dada.tuda.framework.conf.beans;

import dada.tuda.framework.handling.ActionHandler;
import dada.tuda.framework.handling.DomainHandlers;
import dada.tuda.framework.normalization.messages.NormalizedMessage;

@DomainHandlers(domain = "example")
public class MyHandler {
    @ActionHandler(action = "created")
    public void handle(NormalizedMessage message) throws Exception {
        if (message.getObjectId() == null) {
            throw new IllegalStateException("object id is null");
        }
    }
}
