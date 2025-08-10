package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.interfaces.DomainSpecialAction;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.interfaces.OverallAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventActionContext implements IEventActionContext {
    private final Map<String, IEventAction> name2actionMap;
    private final Map<IMessagingDomain, List<IEventAction>> allowedActionsByDomainMap;
    private final List<IEventAction> defaultActions;
    private final DomainContext domainContext;

    public EventActionContext(List<IEventAction> values, DomainContext domainContext) {
        this.domainContext = domainContext;
        defaultActions = new ArrayList<>();
        allowedActionsByDomainMap = new ConcurrentHashMap<>();
        name2actionMap = new ConcurrentHashMap<>();
        for (IEventAction action : values) {
            name2actionMap.putIfAbsent(action.getName().toLowerCase(), action);
            populateAction(action);
            if (action.isCancelable() && !(action instanceof CancelEventActionTemplate) && !action.isQuery()) {
                getOrCreateCancelByAction(action);
            }
        }
    }

    private void populateAction(IEventAction action) {
        IEventAction computeBy = action;
        if (action instanceof CancelEventActionTemplate cancel) {
            computeBy = cancel.getActionToCancel();
        }
        if (computeBy instanceof OverallAction) {
            defaultActions.add(action);
        }
        if (computeBy instanceof DomainSpecialAction specialAction) {
            for (String domainName : specialAction.getAllowedDomainNames()) {
                IMessagingDomain domain = this.domainContext.getByName(domainName);
                allowedActionsByDomainMap.computeIfAbsent(domain, k -> new ArrayList<>()).add(action);
            }
        }

    }

    @Override
    public IEventAction getByName(String name) {
        if (name == null) {
            return null;
        }
        return name2actionMap.computeIfAbsent(name.toLowerCase(), k -> {
            if (CancelEventActionTemplate.nameIsCancel(name)) {
                String originalName = CancelEventActionTemplate.getOriginalNameFromCancel(name);
                return getOrCreateCancelByAction(name2actionMap.get(originalName));
            }
            throw new IllegalArgumentException("Action with name " + name + " not found");
        });

    }

    @Override
    public CancelEventActionTemplate getOrCreateCancelByAction(IEventAction action) {
        if (action instanceof CancelEventActionTemplate alreadyIsCancel) {
            return alreadyIsCancel;

        }
        String cancelName = CancelEventActionTemplate.createNameForCancelByOriginal(action.getName());
        var act = name2actionMap.computeIfAbsent(cancelName, name -> {
            var a = new CancelEventActionTemplate(action);
            populateAction(a);
            return a;
        });
        if (act instanceof CancelEventActionTemplate cancelEventActionTemplate) {
            return cancelEventActionTemplate;
        } else {
            throw new IllegalStateException("Action with name " + cancelName + " is not a CancelEventActionTemplate");
        }
    }


    @Override
    public List<IEventAction> getAllowedActionsByDomian(IMessagingDomain domain) {
        List<IEventAction> defaults = new ArrayList<>(defaultActions);
        defaults.addAll(allowedActionsByDomainMap.getOrDefault(domain, List.of()));
        return defaults;
    }

    @Override
    public boolean isAllowedAction(IMessagingDomain domain, IEventAction action) {
        return defaultActions.contains(action) || allowedActionsByDomainMap.getOrDefault(domain, List.of()).contains(action);
    }

}

