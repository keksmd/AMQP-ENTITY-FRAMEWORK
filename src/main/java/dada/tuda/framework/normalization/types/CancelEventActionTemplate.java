package dada.tuda.framework.normalization.types;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode

public class CancelEventActionTemplate implements IEventAction {
    public static final String CANCEL_SUFFIX = ".cancel";
    private final IEventAction actionToCancel;

    public CancelEventActionTemplate(IEventAction actionToCancel) {
        this.actionToCancel = actionToCancel;
    }

    @Override
    public String name() {
        return actionToCancel.name() + CANCEL_SUFFIX;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public boolean isCancel() {
        return true;
    }
}
