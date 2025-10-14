package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.core.Queue;

import java.util.Set;


public interface QueueContext {

    Queue getQueueByDomain(IMessagingDomain domain);

    void registerQueueForDomain(Queue queue, IMessagingDomain domain);

    Set<Queue> getAllQueues();
}
