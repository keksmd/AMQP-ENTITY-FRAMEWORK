package dada.tuda.framework.annotations;
@EnumBean()
public enum MyTestEnumWithConstructorParameter {
    FIRST("1"),
    SECOND("2");
    private final String value;

    MyTestEnumWithConstructorParameter(String value) {
        this.value = value;
    }
}