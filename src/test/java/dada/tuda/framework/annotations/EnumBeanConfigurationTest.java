package dada.tuda.framework.annotations;

import dada.tuda.framework.configuration.aspect.publiced.EnumBeanConfiguration;
import dada.tuda.framework.configuration.rabbit.ExchangesConfiguration;
import dada.tuda.framework.normalization.types.interfaces.IMessagingAggregate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootTest(classes = {EnumBeanConfiguration.class,EnumBeanConfigurationTest.class,MyTestEnum.class,MyTestEnumWithNoPrefix.class,MyTestEnumWithConstructorParameter.class, ExchangesConfiguration.class})
class EnumBeanConfigurationTest {
    @Autowired
   ApplicationContext context;


    @Autowired(required = false)
    private List<IMessagingAggregate> ens;

    @Test
    void testSimpleEnumBeansAreRegistered() {
        // Создаём контекст с конфигурацией и самим enum-классом
        MyTestEnum firstBean = (MyTestEnum) context.getBean("myTestEnum.FIRST");
        MyTestEnum secondBean = (MyTestEnum) context.getBean("myTestEnum.SECOND");
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
        MyTestEnumWithConstructorParameter firstBeanWithNoPrefix = (MyTestEnumWithConstructorParameter ) context.getBean("myTestEnumWithConstructorParameter.FIRST");
        MyTestEnumWithConstructorParameter secondBeanWithNoPrefix = (MyTestEnumWithConstructorParameter ) context.getBean("myTestEnumWithConstructorParameter.SECOND");
        // Убедимся, что объекты действительно совпадают
        Assertions.assertEquals(MyTestEnumWithConstructorParameter.FIRST, firstBeanWithNoPrefix);
        Assertions.assertEquals(MyTestEnumWithConstructorParameter.SECOND, secondBeanWithNoPrefix);
    }
    @Test
    void testWithEnumBeansAreRegistered() {
      String exchangeName = (String) context.getBean("entityExchangeName");
       org.springframework.amqp.core.Exchange exchange= (org.springframework.amqp.core.Exchange) context.getBean("entityExchange");
       assert exchange.getName().equals(exchangeName);
    }
}
