package dada.tuda.framework.annotations;

import dada.tuda.framework.configuration.aspect.publiced.EnumBeanConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EnumBeanConfigurationTest {

    @Test
    void testEnumBeansAreRegistered() {
        // Создаём контекст с конфигурацией и самим enum-классом
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(
                        EnumBeanConfiguration.class
                );
        MyTestEnum firstBean = (MyTestEnum) context.getBean("MyTestEnum.FIRST");
        MyTestEnum secondBean = (MyTestEnum) context.getBean("MyTestEnum.SECOND");
        MyTestEnumWithNoPrefix firstBeanWithNoPrefix = (MyTestEnumWithNoPrefix) context.getBean("FIRST");
        MyTestEnumWithNoPrefix secondBeanWithNoPrefix = (MyTestEnumWithNoPrefix) context.getBean("SECOND");


        // Убедимся, что объекты действительно совпадают
        Assertions.assertEquals(MyTestEnum.FIRST, firstBean);
        Assertions.assertEquals(MyTestEnum.SECOND, secondBean);
        Assertions.assertEquals(MyTestEnumWithNoPrefix.FIRST, firstBeanWithNoPrefix);
        Assertions.assertEquals(MyTestEnumWithNoPrefix.SECOND, secondBeanWithNoPrefix);

        context.close();
    }
}
