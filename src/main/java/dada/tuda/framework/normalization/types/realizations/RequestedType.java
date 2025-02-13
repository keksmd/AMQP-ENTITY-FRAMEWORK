package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.normalization.types.interfaces.IEventActionType;

public class RequestedType implements IEventActionType {
    private RequestedType() {

    }

    public static RequestedType getInstance() {
        return RequestedType.Singleton.instance;
    }

    @Override
    public String name() {
        return "REQUESTED";
    }

    private static class Singleton {
        static RequestedType instance = new RequestedType();
    }
}
