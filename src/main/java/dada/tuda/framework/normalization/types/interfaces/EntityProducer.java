package dada.tuda.framework.normalization.types.interfaces;

import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.facade.MessagingEntittyRepository;
import dada.tuda.framework.normalization.messages.NormalMessage;
import dada.tuda.framework.normalization.types.realizations.CRUDEventActionTypes;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeoutException;


@RequiredArgsConstructor
public class EntityProducer<Entity> implements MessagingEntittyRepository<Entity> {
    private final MessageSender sender;
    private final EntityContext entityContext;
    private final DescriptorConverter descriptorConverter;
    private final MessageStorage messageStorage;

    @Override
    public void doCommand(Entity entity, IEventAction action, boolean forOthersOnly) {
        var descriptor = this.entityContext.getDescriptorByMessagingEntityClass(entity.getClass());
        NormalMessage msg = descriptorConverter.createFromDescriptor(entity, action, descriptor);
        msg.setActionTypeName(action.getName());
        if (forOthersOnly && !action.isQuery()) messageStorage.storeEventAsProcessed(msg);
        sender.sendUsingType(msg);
    }

    @Override
    public void create(Entity entity, boolean forOtherOnly) {
        this.doCommand(entity, CRUDEventActionTypes.CREATED, forOtherOnly);
    }

    @Override
    public <T> T doQuery(Entity entity, IEventAction action, boolean forOthersOnly, Class<T> responseType) throws TimeoutException {
        var descriptor = this.entityContext.getDescriptorByMessagingEntityClass(entity.getClass());
        if (descriptor == null) {
            throw new IllegalStateException("Descriptor is null for entity: " + entity);
        }
        NormalMessage msg = descriptorConverter.createFromDescriptor(entity, action, descriptor);
        msg.setActionTypeName(action.getName());
        if (forOthersOnly && !action.isQuery()) messageStorage.storeEventAsProcessed(msg);
        return sender.sendRequestUsingType(msg, responseType);

    }

    @Override
    public void delete(Entity entity, boolean forOtherOnly) {
        this.doCommand(entity, CRUDEventActionTypes.DELETED, forOtherOnly);
    }

    @Override
    public <T> T request(Entity entity, Class<T> responseType, boolean forOthersOnly) throws TimeoutException {
        return this.doQuery(entity, CRUDEventActionTypes.REQUESTED, forOthersOnly, responseType);
    }
}
