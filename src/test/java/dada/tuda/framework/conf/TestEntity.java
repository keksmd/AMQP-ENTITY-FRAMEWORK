package dada.tuda.framework.conf;

import dada.tuda.framework.crud.MessagingEntity;
import dada.tuda.framework.crud.extractor.ObjectId;
import dada.tuda.framework.crud.extractor.PayloadMap;
import lombok.Data;

import java.util.Map;

@Data
@MessagingEntity(domain = "example")
public class TestEntity {
    @PayloadMap()
    private Object bim = Map.of("field1", "f1");
    private String id;
    @ObjectId
    private String object;
}
