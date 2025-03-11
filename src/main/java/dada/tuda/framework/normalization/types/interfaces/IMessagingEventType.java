package dada.tuda.framework.normalization.types.interfaces;

import java.io.Serializable;

public interface IMessagingEventType extends Serializable {

    IMessagingDomain getAggregate();

    IEventAction getActionType();

    boolean isQuery();

    String name();


}


