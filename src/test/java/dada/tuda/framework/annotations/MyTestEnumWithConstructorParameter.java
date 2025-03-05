package dada.tuda.framework.annotations;

import dada.tuda.framework.redis.IEnum;

@EnumBean
public enum MyTestEnumWithConstructorParameter implements IEnum {
    FIRST("1"),
    SECOND("2");
    private final String value;

    MyTestEnumWithConstructorParameter(String value) {
        this.value = value;
    }
}