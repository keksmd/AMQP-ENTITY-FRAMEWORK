package dada.tuda.framework.crud;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class SimpleDomain implements IMessagingDomain {
    String name;
}
