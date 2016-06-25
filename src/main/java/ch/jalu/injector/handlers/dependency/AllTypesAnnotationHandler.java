package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.AllTypes;
import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Annotation handler for {@link AllTypes}. Dependencies with this annotation will be
 * assigned a collection of all known subtypes in the project's package.
 */
public class AllTypesAnnotationHandler extends TypeSafeAnnotationHandler<AllTypes> {

    private Reflections reflections;

    public AllTypesAnnotationHandler(String rootPackage) {
        reflections = new Reflections(rootPackage);
    }

    @Override
    public Class<AllTypes> getAnnotationType() {
        return AllTypes.class;
    }

    @Override
    public Object resolveValueSafely(Injector injector, AllTypes annotation,
                                     DependencyDescription dependencyDescription) {
        InjectorUtils.checkNotNull(annotation.value(), "Annotation value may not be null", AllTypes.class);
        Set<?> subTypes = reflections.getSubTypesOf(annotation.value());

        Class<?> rawType = dependencyDescription.getType();
        return ReflectionUtils.toSuitableCollectionType(rawType, subTypes);
    }
}
