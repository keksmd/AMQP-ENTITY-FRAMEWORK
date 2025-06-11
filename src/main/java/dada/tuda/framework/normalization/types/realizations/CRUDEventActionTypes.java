package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.annotations.EnumBean;
import dada.tuda.framework.enums.IEnum;
import dada.tuda.framework.normalization.types.interfaces.OverallAction;

@EnumBean
public enum CRUDEventActionTypes implements OverallAction, IEnum {
    CREATED(false),
    UPDATED(false),
    DELETED(false),
    REQUESTED(true),
    ASYNC_REQUESTED(true);
    private final boolean query;

    CRUDEventActionTypes(boolean query) {
        this.query = query;
    }

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }

    @Override
    public boolean isQuery() {
        return query;
    }
}
