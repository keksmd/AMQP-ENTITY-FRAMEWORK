package dada.tuda.framework.conf;

import dada.tuda.framework.crud.MessagingEntity;
import dada.tuda.framework.crud.extractor.ObjectId;
import lombok.Data;

@Data
@MessagingEntity(domain = "example", queues = "example-2-queue")
public class TestEntity {
    private String field1 = "f1";
    private String id;
    @ObjectId
    private String object;
}
