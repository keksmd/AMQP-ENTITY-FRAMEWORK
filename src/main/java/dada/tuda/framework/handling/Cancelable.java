package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalMessage;

public interface Cancelable {
    void cancel(NormalMessage message) throws Exception;
}
