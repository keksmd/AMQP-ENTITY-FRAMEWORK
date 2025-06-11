package dada.tuda.framework.handling;

import dada.tuda.framework.normalization.messages.NormalizedMessage;

public interface Cancelable {
    void cancel(NormalizedMessage message) throws Exception;
}
