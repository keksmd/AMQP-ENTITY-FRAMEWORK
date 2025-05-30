package dada.tuda.framework.normalization.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import dada.tuda.framework.WholeConfig;
import dada.tuda.framework.conf.RabbitContainerConfig;
import dada.tuda.framework.conf.RedisContainerConfig;
import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.consistency.MessageRepository;
import dada.tuda.framework.consistency.mapper.MessageMapper;
import dada.tuda.framework.crud.MessageJPAEntity;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.CancelEventActionTemplate;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestPropertySource(locations = "classpath:application.yml")
@SpringBootTest(classes = { TestEntity.class, RabbitContainerConfig.class, RedisContainerConfig.class, NormalMessageSerializerTest.class, WholeConfig.class })
@ActiveProfiles("test") // или укажи правильный профиль, если нужен
class NormalMessageSerializerTest {


    @Autowired
    ObjectMapper defaultObjectMapper;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageMapper mapper;

    private static Stream<JsonNormalMessage> provideStringsForIsBlank() {
        var p1 = prototype();
        var p2 = prototype();
        p2.setActionTypeName(new CancelEventActionTemplate(CRUDEventActionTypes.DELETED).name());

        return Stream.of(p1, p2);
    }

    static JsonNormalMessage prototype() {
        return new JsonNormalMessage(
                "object-id-123",
                "actor-123",
                Map.of("key1", "value1"),
                "example",
                CRUDEventActionTypes.CREATED.name());
    }

    @BeforeEach
    public void setUp() {
        messageRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    void testSaveToRepo(JsonNormalMessage original) {
        MessageJPAEntity entity = mapper.toEntity(original);
        entity = messageRepository.save(entity);
        check(entity, original);

        Optional<MessageJPAEntity> fromRepoOpt = messageRepository.findById(entity.getOperationId());
        assertTrue(fromRepoOpt.isPresent());

        JsonNormalMessage restored = mapper.toMessage(fromRepoOpt.get());
        check(restored, original);
    }

    static void check(NormalMessage original, NormalMessage restored) {
        assertEquals(original.getObjectId(), restored.getObjectId());
        assertEquals(original.getPayloadMap(), restored.getPayloadMap());
        assertEquals(original.getActorId(), restored.getActorId());
        assertEquals(original.getOperationId(), restored.getOperationId());
        assertEquals(original.getDomainName(), restored.getDomainName());
        assertEquals(original.getActionTypeName(), restored.getActionTypeName());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    void testLoadToJson(JsonNormalMessage original) {
        String json = defaultObjectMapper.writeValueAsString(original);
        JsonNormalMessage restored = defaultObjectMapper.readValue(json, JsonNormalMessage.class);

        check(restored, original);
    }


}