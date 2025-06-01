package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;

public interface CancelEventActionContext {
    CancelEventActionTemplate getOrCreateCancelByAction(IEventAction action);
}
