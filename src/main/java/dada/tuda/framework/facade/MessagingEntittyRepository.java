package dada.tuda.framework.facade;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;

public interface MessagingEntittyRepository<Entity> {

    default <T> T doAction(Entity entity, IEventAction action) {
        return doAction(entity, action, false);
    }

    <T> T doAction(Entity entity, IEventAction action, boolean forOthersOnly);

    default void create(Entity entity) {
        create(entity, false);
    }

    void create(Entity entity, boolean forOtherOnly);

    default void delete(Entity entity) {
        delete(entity, false);
    }

    void delete(Entity entity, boolean forOtherOnly);

    <T> T request(Entity entity);
}
