package dada.tuda.framework.annotations;

@EnumBean(enumNamePrefix = "true")  // Добавляем префикс (название enum-класса)
public enum MyTestEnum {
    FIRST,
    SECOND
}
@EnumBean(enumNamePrefix = "false")  // Добавляем префикс (название enum-класса)
enum MyTestEnumWithNoPrefix {
    FIRST,
    SECOND
}