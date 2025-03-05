package dada.tuda.framework.annotations;

import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import dada.tuda.framework.redis.IEnum;

@EnumBean
public enum MyTestEnumExchange implements IMessagingAggregate, IEnum {
    ENTITY;
    @Override
    public String getName() {
        return name().toLowerCase();
    }
}