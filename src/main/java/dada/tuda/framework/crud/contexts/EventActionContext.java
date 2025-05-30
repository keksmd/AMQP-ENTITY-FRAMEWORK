package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventActionContext {
    private final Map<String, IEventAction> actionMap;

    public EventActionContext(List<IEventAction> values) {
        actionMap = new ConcurrentHashMap<>();
        for (IEventAction action : values) {
            actionMap.put(action.name(), action);
        }
    }

    public IEventAction getByName(String name) {
        if (name.endsWith(CancelEventActionTemplate.CANCEL_SUFFIX)) {
            String nameOfCanceling = name.substring(0, name.length() - CancelEventActionTemplate.CANCEL_SUFFIX.length());
            IEventAction canceling = actionMap.get(nameOfCanceling);
            if (canceling == null) {
                throw new IllegalStateException("No action found for canceling: " + nameOfCanceling);
            }
            return actionMap.computeIfAbsent(name, n -> new CancelEventActionTemplate(canceling));
        } else {
            return actionMap.get(name);
        }
    }
}
