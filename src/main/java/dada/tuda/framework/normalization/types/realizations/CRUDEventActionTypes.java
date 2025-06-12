package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.annotations.EnumBean;
import dada.tuda.framework.enums.IEnum;
import dada.tuda.framework.normalization.types.interfaces.OverallAction;
import lombok.Getter;

@EnumBean
public enum CRUDEventActionTypes implements OverallAction, IEnum {
    CREATED(false),
    UPDATED(false),
    DELETED(false),
    REQUESTED(true, false),
    REQUESTED_LIST(true, false),
    ASYNC_REQUESTED(true, false);
    private final boolean query;
    @Getter
    private final boolean cancelable;

    CRUDEventActionTypes(boolean query, boolean cancelable) {
        this.query = query;
        this.cancelable = cancelable;
    }

    CRUDEventActionTypes(boolean query) {
        this.query = query;
        this.cancelable = true;
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
