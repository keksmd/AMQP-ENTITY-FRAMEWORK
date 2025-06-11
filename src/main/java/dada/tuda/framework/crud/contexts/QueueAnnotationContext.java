package dada.tuda.framework.crud.contexts;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import org.springframework.amqp.rabbit.annotation.Queue;

import java.util.List;

public interface QueueAnnotationContext {

    List<Queue> getQueueListByDomain(IMessagingDomain domain);

    void registerQueueForDomain(Queue queue, IMessagingDomain domain);
}
