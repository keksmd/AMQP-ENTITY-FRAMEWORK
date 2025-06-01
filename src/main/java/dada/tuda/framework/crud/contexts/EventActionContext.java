package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventActionContext implements IEventActionContext {
    private final Map<String, IEventAction> name2actionMap;


    public EventActionContext(List<IEventAction> values) {
        name2actionMap = new ConcurrentHashMap<>();
        for (IEventAction action : values) {
            name2actionMap.put(action.name(), action);
        }
    }

    @Override
    public IEventAction getByName(String name) {
        return name2actionMap.get(name);
    }

    @Override
    public List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain) {
        return name2actionMap.values().stream().toList();
    }
}

