package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
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
            if (action.isCancelable() && !(action instanceof CancelEventActionTemplate) && !action.isQuery()) {
                getOrCreateCancelByAction(action);
            }
        }
    }

    @Override
    public IEventAction getByName(String name) {
        if (CancelEventActionTemplate.nameIsCancel(name)) {
            String originalName = CancelEventActionTemplate.getOriginalNameFromCancel(name);
            return getOrCreateCancelByAction(name2actionMap.get(originalName));
        } else {
            return name2actionMap.get(name);
        }
    }

    @Override
    public CancelEventActionTemplate getOrCreateCancelByAction(IEventAction action) {
        String cancelName = CancelEventActionTemplate.createNameForCancelByOriginal(action.name());
        var act = name2actionMap.computeIfAbsent(cancelName, name -> new CancelEventActionTemplate(action));
        if (act instanceof CancelEventActionTemplate cancelEventActionTemplate) {
            return cancelEventActionTemplate;
        } else {
            throw new IllegalStateException("Action with name " + cancelName + " is not a CancelEventActionTemplate");
        }
    }


    @Override
    public List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain) {
        return name2actionMap.values().stream().toList();
    }

}

