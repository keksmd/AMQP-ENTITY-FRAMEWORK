package dada.tuda.framework.consistency;

import dada.tuda.framework.crud.MessageJPAEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepository extends CrudRepository<MessageJPAEntity, String> {

    List<MessageJPAEntity> getAllByTtlEquals(Long ttl);

    List<MessageJPAEntity> getAllByTtlNotNull();

    List<MessageJPAEntity> getAllByDomainName(String domainName);
}
