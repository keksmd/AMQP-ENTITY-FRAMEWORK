package dada.tuda.framework.consistency;

import dada.tuda.framework.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<EventEntity, String> {

}
