package dada.tuda.framework.consistency;

import dada.tuda.framework.entity.EventEntity;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class RedisEventStorager implements EventStorager {
    private final EventRepository eventRepository;
    private final MessageEntityMapper mapper;

    @Override
    public void save(AbstractNormalMessage message) {
        try {
            eventRepository.save(new EventEntity(message));
            log.debug("saved event processed {}", message.getOperationId());
        } catch (Exception e) {
            log.error("failed to save event processed {}", e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public AbstractNormalMessage getByID(String operationId) {
        return eventRepository.findById(operationId).map(mapper::toMessage).orElse(null);
    }
}
