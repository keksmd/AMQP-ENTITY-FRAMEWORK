package dada.tuda.framework.consistency;

import dada.tuda.framework.normalization.AbstractNormalMessage;

import javax.naming.OperationNotSupportedException;

public interface EventStorager {
    AbstractNormalMessage getByID(String operationId) throws OperationNotSupportedException;

    void save(AbstractNormalMessage message);

    boolean isEnabled();
}
