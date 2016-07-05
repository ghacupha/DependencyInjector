package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre construct handler that maps abstract classes to a previously registered implementation.
 */
public class ImplementationClassHandler extends AbstractCountingHandler implements PreConstructHandler {

    private Map<Class<?>, Class<?>> classMap = new HashMap<>();

    /**
     * Registers a child class to use when the parent is requested to be instantiated.
     *
     * @param parent the parent class to "redirect"
     * @param child the child class to use
     * @param <T> the parent's type
     */
    public <T> void register(Class<T> parent, Class<? extends T> child) {
        classMap.put(parent, child);
    }

    @Override
    public <T> Class<? extends T> accept(Class<T> clazz) {
        increment();
        return getImplClass(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> getImplClass(Class<T> clazz) {
        Class<? extends T> child = (Class<? extends T>) classMap.get(clazz);
        if (child == null) {
            return clazz;
        }
        return getImplClass(child);
    }
}