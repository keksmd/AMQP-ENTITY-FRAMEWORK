package dada.tuda.framework.enums;

import dada.tuda.framework.annotations.EnumBean;
import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;

@EnumBean
public enum MyTestEnumExchange implements IMessagingDomain, IEnum {
    ENTITY;

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    @Override
    public boolean isCreateDefaultBindings() {
        return true;
    }

    @Override
    public Long getTtl() {
        return 100L;
    }

    @Override
    public void setTtl(Long ttl) {

    }
}