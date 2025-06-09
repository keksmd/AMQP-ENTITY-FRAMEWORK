package dada.tuda.framework.normalization.types.interfaces;

public interface IEventAction {
    String name();

    boolean isQuery();

    default boolean isCancelable() {
        return true;
    }
}

