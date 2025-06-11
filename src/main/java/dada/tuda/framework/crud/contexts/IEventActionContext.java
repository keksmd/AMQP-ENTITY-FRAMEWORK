package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;

public interface IEventActionContext {

    IEventAction getByName(String name);

    CancelEventActionTemplate getOrCreateCancelByAction(IEventAction action);

    default void init() {
    }

    List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain);

    boolean isAllowedAction(IMessagingDomain domain, IEventAction action);
}
