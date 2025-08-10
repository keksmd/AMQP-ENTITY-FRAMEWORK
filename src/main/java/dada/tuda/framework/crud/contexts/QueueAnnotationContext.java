package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.rabbit.annotation.Queue;

public interface QueueAnnotationContext {

    Queue getQueueByDomain(IMessagingDomain domain);

    void registerQueueForDomain(Queue queue, IMessagingDomain domain);
}
