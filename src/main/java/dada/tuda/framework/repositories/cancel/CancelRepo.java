package dada.tuda.framework.repositories.cancel;

import dada.tuda.framework.facade.MessagingEntittyRepository;
import dada.tuda.framework.normalization.types.realizations.CancelPayload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "dada.tuda.messaging.saga.enabled", havingValue = "true")
@Repository
public interface CancelRepo extends MessagingEntittyRepository<CancelPayload> {
}
