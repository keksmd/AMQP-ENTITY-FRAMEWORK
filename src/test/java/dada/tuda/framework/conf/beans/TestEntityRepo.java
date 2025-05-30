package dada.tuda.framework.conf.beans;

import dada.tuda.framework.conf.TestEntity;
import dada.tuda.framework.facade.MessagingEntittyRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepo extends MessagingEntittyRepository<TestEntity> {

}
