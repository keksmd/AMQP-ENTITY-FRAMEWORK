package dada.tuda.framework.normalization.types.interfaces;

public interface IMessagingDomain {
    default String getExchangeName() {
        return this.getKey() + "-exchange";
    }

    default String getKey() {
        return this.getName().toLowerCase();
    }

    String getName();

    boolean isCreateDefaultBindings();

    Long getTtl();

    void setTtl(Long ttl);


}
