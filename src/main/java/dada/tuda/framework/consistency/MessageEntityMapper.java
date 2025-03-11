package dada.tuda.framework.consistency;

import dada.tuda.framework.entity.EventEntity;
import dada.tuda.framework.normalization.AbstractNormalMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MessageEntityMapper {
    EventEntity toEntity(AbstractNormalMessage message);
    AbstractNormalMessage toMessage(EventEntity entity);
}
