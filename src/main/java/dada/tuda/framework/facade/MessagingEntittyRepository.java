package dada.tuda.framework.facade;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;

import java.util.concurrent.TimeoutException;

public interface MessagingEntittyRepository<Entity> {

    default void doCommand(Entity entity, IEventAction action) {
        doCommand(entity, action, false);
    }

    void doCommand(Entity entity, IEventAction action, boolean forOthersOnly);

    default void create(Entity entity) {
        create(entity, false);
    }

    void create(Entity entity, boolean forOtherOnly);

    <T> T doQuery(Entity entity, IEventAction action, boolean forOthersOnly, Class<T> responseType) throws TimeoutException;

    default void delete(Entity entity) {
        delete(entity, false);
    }

    void delete(Entity entity, boolean forOtherOnly);

    default <T> T request(Entity entity, Class<T> responseType) throws TimeoutException {
        return request(entity, responseType, true);
    }

    <T> T request(Entity entity, Class<T> responseType, boolean forOthersOnly) throws TimeoutException;
}
