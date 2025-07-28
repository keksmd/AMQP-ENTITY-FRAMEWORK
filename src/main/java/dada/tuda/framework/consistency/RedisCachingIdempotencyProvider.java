package dada.tuda.framework.consistency;

import dada.tuda.framework.consistency.mapper.RedisMapper;
import dada.tuda.framework.normalization.messages.NormalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RedisCachingIdempotencyProvider implements MessageStorage {
    private final MessageRepository messageRepository;
    private final RedisMapper mapper;


    @Override
    public NormalMessage getByID(String operationId) {
        return messageRepository.findById(operationId).orElse(null);
    }



    @Override
    public boolean isProcessedById(String id) {
        try {
            return messageRepository.existsById(id);
        } catch (Exception e) {
            log.error("failed to check event processed,input id={}\n{}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public void storeEventAsProcessed(NormalMessage message) {
        try {
            var entity = mapper.toEntity(message);
            if (entity.getTtl() != 0) {
                messageRepository.save(entity);
                log.debug("saved event processed {}", message.getOperationId());
            } else {
                log.debug("skipped saving event processed with ttl=0,operationId={}", message.getOperationId());
            }
        } catch (Exception e) {
            log.error("failed to save event processed", e);
        }
    }

    @Override
    public void clear() {
        messageRepository.deleteAll();
    }


}
