package dada.tuda.framework.normalization.types.interfaces;

public interface IEventAction {
    String getName();

    boolean isQuery();

    default boolean isCancelable() {
        return true;
    }
}

