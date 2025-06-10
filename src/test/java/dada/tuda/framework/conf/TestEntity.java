package dada.tuda.framework.conf;

import dada.tuda.framework.crud.MessagingEntity;
import dada.tuda.framework.crud.extractor.ObjectId;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.Queue;

@Data
@MessagingEntity(domain = "example", queues = @Queue(name = "${example-3-queue.name}"))
public class TestEntity {
    private String field1 = "f1";
    private String id;
    @ObjectId
    private String object;
}
