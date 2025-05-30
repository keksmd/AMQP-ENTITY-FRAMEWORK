package dada.tuda.framework.consistency;

import dada.tuda.framework.crud.MessageJPAEntity;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<MessageJPAEntity, String> {

}
