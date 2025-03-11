package dada.tuda.framework.annotations;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import dada.tuda.framework.redis.IEnum;

@EnumBean
public enum MyTestEnumExchange implements IMessagingDomain, IEnum {
    ENTITY;
    @Override
    public String getName() {
        return name().toLowerCase();
    }
}