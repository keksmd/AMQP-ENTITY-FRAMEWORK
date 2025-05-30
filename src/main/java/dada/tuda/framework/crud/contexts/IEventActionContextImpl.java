package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class IEventActionContextImpl implements IEventActionContext {
    private final List<IEventAction> iEventActions;

    @Override
    public List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain) {
        return iEventActions;
    }
}
