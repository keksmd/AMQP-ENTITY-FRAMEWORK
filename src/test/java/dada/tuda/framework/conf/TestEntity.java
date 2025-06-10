package dada.tuda.framework.conf;

import dada.tuda.framework.crud.ListenableQueue;
import dada.tuda.framework.crud.MessagingEntity;
import dada.tuda.framework.crud.extractor.ObjectId;
import dada.tuda.framework.crud.extractor.PayloadMap;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.Queue;

import java.util.Map;

@Data
@MessagingEntity(domain = "example", queues = @ListenableQueue(@Queue(name = "${example-3-queue.name}")))
public class TestEntity {
    @PayloadMap()
    private Object bim = Map.of("field1", "f1");
    // private String field1 = "f1";
    private String id;
    @ObjectId
    private String object;
}
