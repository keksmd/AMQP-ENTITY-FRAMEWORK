package dada.tuda.framework.enums;

import dada.tuda.framework.annotations.EnumBean;
import lombok.Getter;

@EnumBean
@Getter
public enum MyTestEnumWithConstructorParameter implements IEnum {
    FIRST("1"),
    SECOND("2");
    private final String value;

    MyTestEnumWithConstructorParameter(String value) {
        this.value = value;
    }
}