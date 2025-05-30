package dada.tuda.framework.enums;

import dada.tuda.framework.annotations.EnumBean;

@EnumBean(classnamePrefix = "false")
public enum MyTestEnumWithNoPrefix implements IEnum {
    FIRST,
    SECOND
}