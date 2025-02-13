package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.normalization.types.interfaces.IEventActionType;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;


public class CancellingEvent implements IMessagingEventType {
    private final IMessagingAggregate aggregate;
    private final IEventActionType type;

    public CancellingEvent(IMessagingAggregate aggregate, IEventActionType type) {
        this.aggregate = aggregate;
        this.type = type;
    }

    @Override
    public IMessagingAggregate getAggregate() {
        return aggregate;
    }

    @Override
    public IEventActionType getActionType() {
        return type;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public String name() {
        return "CANCEL";
    }


}
