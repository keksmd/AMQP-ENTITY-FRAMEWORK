package dada.tuda.framework.annotations;

import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;

@EnumBean(enumNamePrefix = "false")
public enum MyTestEnumExchange implements IMessagingAggregate {
    ENTITY;
    @Override
    public String getName() {
        return name().toLowerCase();
    }
}