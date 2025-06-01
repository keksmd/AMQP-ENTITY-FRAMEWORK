package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;

public interface IEventActionContext {

    IEventAction getByName(String name);

    List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain);
}
