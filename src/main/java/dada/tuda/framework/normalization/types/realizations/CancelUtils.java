package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;

public class CancelUtils {
    public static final IMessagingDomain CANCELLED_DOMAIN = () -> "CANCELLED";
    public static final IEventAction CANCELLED_ACTION = () -> "CANCELLED";
   public static IMessagingEventType CANCELLING_EVENT_TYPE = new CancellingEvent();

    private CancelUtils() {
    }

    public static class CancellingEvent implements IMessagingEventType {

        @Override
        public IMessagingDomain getDomain() {
            return CancelUtils.CANCELLED_DOMAIN;
        }

        @Override
        public IEventAction getActionType() {
            return CancelUtils.CANCELLED_ACTION;
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
