package dada.tuda.framework.normalization.types.interfaces;

import java.io.Serializable;

public interface IMessagingEventType extends Serializable {

    IMessagingAggregate getAggregate();

    IEventActionType getActionType();

    boolean isQuery();

    String name();


}


