package dada.tuda.framework.normalization.types.interfaces;

import java.io.Serializable;

public interface IMessagingEventType extends Serializable {

    IMessagingDomain getDomain();

    IEventAction getActionType();

    boolean isQuery();
    default String toRoutingKey(){
        return this.getDomain().getKey() + "." + this.getActionType().name().toLowerCase();
    }
    String name();


}


