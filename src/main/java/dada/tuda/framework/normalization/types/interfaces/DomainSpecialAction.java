package dada.tuda.framework.normalization.types.interfaces;

import java.util.List;

public interface DomainSpecialAction extends IEventAction {
    List<String> getAllowedDomainNames();
}
