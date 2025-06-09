package dada.tuda.framework.normalization.types;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CancelEventActionTemplate implements IEventAction {
    private static final String CANCEL_SUFFIX = ".cancel";
    private final String name;

    public CancelEventActionTemplate(IEventAction actionToCancel) {
        name = createNameForCancelByOriginal(actionToCancel.name());
    }

    public static String createNameForCancelByOriginal(String originalActionName) {
        return originalActionName + CANCEL_SUFFIX;
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
        return name;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

}
