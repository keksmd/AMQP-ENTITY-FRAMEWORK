package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.normalization.types.interfaces.IEventActionType;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;

public class CancelUtils {
    private static final IMessagingAggregate aggregate = () -> "CANCELLED";
    private static final IEventActionType type = () -> "CANCELED";
    private static CancellingEvent cancellingEvent = new CancellingEvent();

    private CancelUtils() {
    }

    public static IMessagingEventType getCancellingEventType() {
        return cancellingEvent;
    }

    public static IMessagingAggregate getCancelAggregate() {
        return aggregate;
    }

    public static IEventActionType getCancelActionType() {
        return type;
    }

    public static class CancellingEvent implements IMessagingEventType {

        @Override
        public IMessagingAggregate getAggregate() {
            return CancelUtils.aggregate;
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
}
