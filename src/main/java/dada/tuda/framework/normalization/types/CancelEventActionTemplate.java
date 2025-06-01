package dada.tuda.framework.normalization.types;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CancelEventActionTemplate implements IEventAction {
    private static final String CANCEL_SUFFIX = ".cancel";
    private final IEventAction actionToCancel;

    public CancelEventActionTemplate(IEventAction actionToCancel) {
        this.actionToCancel = actionToCancel;
    }

    public static String getOriginalNameFromCancel(String cancelName) {
        if (!nameIsCancel(cancelName)) {
            throw new IllegalArgumentException("Provided name is not a cancel action: " + cancelName);
        }
        return cancelName.substring(0, cancelName.length() - CANCEL_SUFFIX.length());
    }

    public static boolean nameIsCancel(String name) {
        return name.endsWith(CANCEL_SUFFIX);
    }

    @Override
    public String name() {
        return createNameForCancelByOriginal(actionToCancel.name());
    }

    public static String createNameForCancelByOriginal(String originalActionName) {
        return originalActionName + CANCEL_SUFFIX;
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
