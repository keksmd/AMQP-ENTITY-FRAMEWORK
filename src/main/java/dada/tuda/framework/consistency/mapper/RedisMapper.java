package dada.tuda.framework.consistency.mapper;

import dada.tuda.framework.crud.MessageJPAEntity;
import dada.tuda.framework.crud.contexts.DomainContext;
import dada.tuda.framework.normalization.messages.NormalMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RedisMapper {
    public DomainContext domainContext;

    @Mapping(target = "ttl", expression = "java(domainContext.getByName(message.getDomainName())!=null?(domainContext.getByName(message.getDomainName()).getTtl()):60000L)")
    public abstract MessageJPAEntity toEntity(NormalMessage message);


}
