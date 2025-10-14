package dada.tuda.framework.crud.contexts;

import org.springframework.amqp.core.Binding;

import java.util.Set;


public interface BindingContext {

    Set<Binding> getBindings();

    void registerBinding(Binding binding);
}
