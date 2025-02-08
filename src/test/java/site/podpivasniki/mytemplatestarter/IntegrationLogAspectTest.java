package site.podpivasniki.mytemplatestarter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import site.podpivasniki.mytemplatestarter.logging.IntegrationLogAspect;

@SpringBootTest(classes = IntegrationLogAspectTest.TestConfig.class)
@EnableAspectJAutoProxy
class IntegrationLogAspectTest {

  @Autowired
  private TestService testService;
  private ListAppender<ILoggingEvent> logAppender;

  @BeforeEach
  void setupLogger() {
    Logger logger = (Logger) LoggerFactory.getLogger("IntegrationLogger");

    logAppender = new ListAppender<>();
    logAppender.start();

    logger.addAppender(logAppender);
    logger.setLevel(Level.INFO);
  }

  @Test
  void testIntegrationLogAspect() throws Exception {
    String result = testService.testMethod("Hello");

    assertThat(result).isEqualTo("Processed: Hello");

    List<ILoggingEvent> logsList = logAppender.list;

    assertThat(logsList).anySatisfy(event -> {
      String actualJson = event.getFormattedMessage();
      String expectedJson = """
          {
            "rs": "\\"Processed: Hello\\"",
            "eventType": "TestEventType",
            "rq":  "{\\"input\\":\\"Hello\\"}"
          }
          """;

      JSONAssert.assertEquals(expectedJson, actualJson, false);
    });
  }

  @Test
  void testIntegrationLogAspectThrowEx() throws Exception {
    assertThatThrownBy(() -> testService.testMethodThrowEx("Hello"));

    List<ILoggingEvent> logsList = logAppender.list;

    assertThat(logsList).anySatisfy(event -> {
      String actualJson = event.getFormattedMessage();
      String expectedJson = """
          {
            "error": "Ex",
            "eventType": "TestEventType",
            "rq": "{\\"input\\":\\"Hello\\"}"
          }
          """;

      JSONAssert.assertEquals(expectedJson, actualJson, false);
    });
  }

  @Configuration
  static class TestConfig {

    @Bean
    public TestService testService() {
      return new TestService();
    }

    @Bean
    public IntegrationLogAspect integrationLogAspect() {
      return new IntegrationLogAspect();
    }
  }
}
