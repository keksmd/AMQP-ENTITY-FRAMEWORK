package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.consistency.MessageStorage;
import dada.tuda.framework.crud.DescriptorConverter;
import dada.tuda.framework.crud.contexts.EntityContext;
import dada.tuda.framework.facade.MessageSender;
import dada.tuda.framework.facade.MessagingEntittyRepository;
import dada.tuda.framework.normalization.messages.NormalizedMessage;
import dada.tuda.framework.normalization.types.interfaces.IEventAction;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EntityProducer<Entity> implements MessagingEntittyRepository<Entity> {
    private final MessageSender sender;
    private final EntityContext entityContext;
    private final DescriptorConverter descriptorConverter;
    private final MessageStorage messageStorage;

    @Override
    public <T> T doAction(Entity entity, IEventAction action, boolean forOthersOnly) {
        var descriptor = this.entityContext.getDescriptorByMessagingEntityClass(entity.getClass());
        NormalizedMessage msg = descriptorConverter.createFromDescriptor(entity, action, descriptor);
        if (forOthersOnly && !action.isQuery()) messageStorage.storeEventAsProcessed(msg);
        if (action.isQuery()) {
            return sender.sendRequestUsingType(msg);
        } else {
            sender.sendUsingType(msg);
            return null;
        }
    }

    @Override
    public void create(Entity entity, boolean forOtherOnly) {
        this.doAction(entity, CRUDEventActionTypes.CREATED, forOtherOnly);
    }

    @Override
    public void delete(Entity entity, boolean forOtherOnly) {
        this.doAction(entity, CRUDEventActionTypes.DELETED, forOtherOnly);
    }


    @Override
    public <T> T request(Entity entity) {
        return doAction(entity, CRUDEventActionTypes.REQUESTED);
    }

}
