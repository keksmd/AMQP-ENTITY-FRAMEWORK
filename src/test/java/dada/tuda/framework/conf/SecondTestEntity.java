package dada.tuda.framework.conf;

import dada.tuda.framework.crud.MessagingEntity;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.Queue;

@Data
@MessagingEntity(domain = "example", queues = @Queue(name = "example-4-queue}"))
public class SecondTestEntity {
    private String id;
}
