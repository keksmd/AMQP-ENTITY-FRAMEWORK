package dada.tuda.framework.consistency.mapper;

import dada.tuda.framework.crud.MessageJPAEntity;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.crud.contexts.IEventActionContext;
import dada.tuda.framework.normalization.messages.JsonNormalMessage;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MessageMapper {
    @Autowired
    public DomainContext domainContext;
    @Autowired
    public IEventActionContext eventActionContext;

    @Mapping(target = "ttl", expression = "java(domainContext.getByName(message.getDomainName()).getTtl())")
    public abstract MessageJPAEntity toEntity(NormalMessage message);

    public abstract JsonNormalMessage toMessage(MessageJPAEntity entity);

    public abstract JsonNormalMessage toMessageFromNormal(NormalMessage entity);


}
