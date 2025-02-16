package dada.tuda.framework.annotations;

import dada.tuda.framework.configuration.aspect.publiced.EnumBeanConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EnumBeanConfigurationTest {
    AnnotationConfigApplicationContext context;

    @AfterEach
    void end() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(EnumBeanConfiguration.class);
    }

    @Test
    void testSimpleEnumBeansAreRegistered() {
        // Создаём контекст с конфигурацией и самим enum-классом
        MyTestEnum firstBean = (MyTestEnum) context.getBean("MyTestEnum.FIRST");
        MyTestEnum secondBean = (MyTestEnum) context.getBean("MyTestEnum.SECOND");
        // Убедимся, что объекты действительно совпадают
        Assertions.assertEquals(MyTestEnum.FIRST, firstBean);
        Assertions.assertEquals(MyTestEnum.SECOND, secondBean);
    }

    @Test
    void testWithNoPrefixEnumBeansAreRegistered() {
        // Создаём контекст с конфигурацией и самим enum-классом
        MyTestEnumWithNoPrefix firstBeanWithNoPrefix = (MyTestEnumWithNoPrefix) context.getBean("FIRST");
        MyTestEnumWithNoPrefix secondBeanWithNoPrefix = (MyTestEnumWithNoPrefix) context.getBean("SECOND");
        // Убедимся, что объекты действительно совпадают
        Assertions.assertEquals(MyTestEnumWithNoPrefix.FIRST, firstBeanWithNoPrefix);
        Assertions.assertEquals(MyTestEnumWithNoPrefix.SECOND, secondBeanWithNoPrefix);
    }
    @Test
    void testWithConstructorParameterEnumBeansAreRegistered() {
        // Создаём контекст с конфигурацией и самим enum-классом
        MyTestEnumWithConstructorParameter firstBeanWithNoPrefix = (MyTestEnumWithConstructorParameter ) context.getBean("MyTestEnumWithConstructorParameter.FIRST");
        MyTestEnumWithConstructorParameter secondBeanWithNoPrefix = (MyTestEnumWithConstructorParameter ) context.getBean("MyTestEnumWithConstructorParameter.SECOND");
        // Убедимся, что объекты действительно совпадают
        Assertions.assertEquals(MyTestEnumWithConstructorParameter .FIRST, firstBeanWithNoPrefix);
        Assertions.assertEquals(MyTestEnumWithConstructorParameter .SECOND, secondBeanWithNoPrefix);
    }
}
