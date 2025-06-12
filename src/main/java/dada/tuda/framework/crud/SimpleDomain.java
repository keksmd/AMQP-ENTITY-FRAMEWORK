package dada.tuda.framework.crud;

import dada.tuda.framework.normalization.types.interfaces.IMessagingDomain;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode
@ToString
public class SimpleDomain implements IMessagingDomain {
    String name;
    @Setter
    @Accessors(chain = true)
    private boolean createDefaultBindings;
    @Setter
    @Accessors(chain = true)
    private Long ttl;

    public SimpleDomain(String name) {
        this.name = name;
    }

}
