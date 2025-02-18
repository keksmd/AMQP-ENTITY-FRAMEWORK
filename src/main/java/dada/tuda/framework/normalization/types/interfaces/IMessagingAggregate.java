package dada.tuda.framework.normalization.types.interfaces;

public interface IMessagingAggregate {


    default String getExchangeName(){
        return this.getName().toLowerCase()+"-exchange";
    }

    default String getKey() {
        return this.getName().toLowerCase();
    }

    String getName();


}
