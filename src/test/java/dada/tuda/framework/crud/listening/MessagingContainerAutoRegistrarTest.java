package dada.tuda.framework.crud.listening;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.WholeAutoConfiguration;
import dada.tuda.framework.conf.RabbitContainerConfig;
import dada.tuda.framework.conf.RedisContainerConfig;
import dada.tuda.framework.conf.RepoConfig;
import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.conf.beans.MyHandler;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { RepoConfig.class, MyHandler.class, TestEntity.class, RabbitContainerConfig.class, RedisContainerConfig.class, WholeAutoConfiguration.class }, properties = "dada.tuda.framework.messaging.decompose-routing-key=true")
class MessagingContainerAutoRegistrarTest {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String jsonPayload = """
            {"operationId":"OP_ID",
            "objectId":"1"
            }""".replaceAll("\n", "");
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @SneakyThrows
    @Test
    void testSettingDomainAndActionTypeFromRabbitEntities() {
        Message msg = new Message(jsonPayload.getBytes(StandardCharsets.UTF_8));
        msg.getMessageProperties().getHeaders().put("__TypeId__", JsonNormalMessage.class.getName());

        Future<Message> fut = CompletableFuture.supplyAsync(() ->
                rabbitTemplate.sendAndReceive("example-exchange", "example.requested", msg)
        );
        var ans = fut.get();
        assertNotNull(ans);
        var normal = objectMapper.readValue(new String(ans.getBody()), JsonNormalMessage.class);
        assertNotNull(normal);
        assertEquals("1", normal.getPayloadMap().get("id"),
                "Payload should contain id=1, but was: " + normal.getPayloadMap().get("id"));

    }


}