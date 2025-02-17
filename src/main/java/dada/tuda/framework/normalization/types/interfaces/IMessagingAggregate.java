package dada.tuda.framework.normalization.types.interfaces;

public interface IMessagingAggregate {


    String getExchangeName();

    public default String getKey() {
        return this.getName().toLowerCase();
    }

    String getName();


}
