package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface MessageHandler {

    Object handle(NormalMessage message) throws Exception;
}
