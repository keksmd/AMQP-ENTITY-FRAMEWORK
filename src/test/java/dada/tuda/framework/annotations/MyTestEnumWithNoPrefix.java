package dada.tuda.framework.annotations;

import dada.tuda.framework.redis.IEnum;

@EnumBean(classnamePrefix = "false")
public enum MyTestEnumWithNoPrefix implements IEnum {
    FIRST,
    SECOND
}