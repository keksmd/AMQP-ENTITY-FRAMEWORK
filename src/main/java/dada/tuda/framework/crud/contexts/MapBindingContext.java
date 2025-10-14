package dada.tuda.framework.crud.contexts;

import org.springframework.amqp.core.Binding;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapBindingContext implements BindingContext {
    private final Set<Binding> bindings = ConcurrentHashMap.newKeySet();

    @Override
    public Set<Binding> getBindings() {
        return bindings;
    }

    @Override
    public void registerBinding(Binding binding) {
        if (binding != null) {
            bindings.add(binding);
        }

    }
}
