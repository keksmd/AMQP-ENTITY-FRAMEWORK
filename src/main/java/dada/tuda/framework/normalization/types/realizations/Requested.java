package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.normalization.types.interfaces.IEventAction;

public class Requested implements IEventAction {
    private Requested() {

    }

    public static Requested getInstance() {
        return Requested.Singleton.instance;
    }

    @Override
    public String name() {
        return "REQUESTED";
    }

    private static class Singleton {
        static Requested instance = new Requested();
    }
}
