package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;

/**
 * The injector's default instantiation method; injects constructor and fields.
 *
 * @see StandardInjectionProvider
 */
public class StandardInjection<T> implements Resolution<T> {

    private final Constructor<T> constructor;
    private final List<Field> fields;
    private SoftReference<List<ObjectIdentifier>> dependencies;

    /**
     * Constructs a standard injection object.
     *
     * @param constructor the constructor to create objects with
     * @param fields the fields to inject after instantiation
     */
    public StandardInjection(Constructor<T> constructor, List<Field> fields) {
        this.constructor = constructor;
        this.fields = fields;
    }

    @Override
    public List<ObjectIdentifier> getDependencies() {
        List<ObjectIdentifier> depList = dependencies == null ? null : dependencies.get();
        if (depList == null) {
            List<ObjectIdentifier> constructorDeps = buildConstructorDependencies();
            List<ObjectIdentifier> fieldDeps = buildFieldDependencies();

            depList = new ArrayList<>(constructorDeps.size() + fieldDeps.size());
            depList.addAll(constructorDeps);
            depList.addAll(fieldDeps);
            this.dependencies = new SoftReference<>(depList);
        }
        return depList;
    }

    @Override
    public T instantiateWith(Object... values) {
        // Check no null values & correct size
        InjectorUtils.checkArgument(values.length == constructor.getParameterTypes().length + fields.size(),
            "Number of values does not correspond to the expected number");

        // Constructor injection
        final int constructorParams = constructor.getParameterTypes().length;
        final List<Object> constructorValues = Arrays.asList(values).subList(0, constructorParams);
        T instance = ReflectionUtils.newInstance(constructor, constructorValues.toArray());

        // Field injection
        for (int i = 0; i < fields.size(); ++i) {
            ReflectionUtils.setField(fields.get(i), instance, values[i + constructorParams]);
        }
        return instance;
    }

    @Override
    public boolean isInstantiation() {
        return true;
    }

    private List<ObjectIdentifier> buildConstructorDependencies() {
        final Type[] parameters = constructor.getGenericParameterTypes();
        final Annotation[][] annotations = constructor.getParameterAnnotations();

        List<ObjectIdentifier> dependencies = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; ++i) {
            dependencies.add(new ObjectIdentifier(SINGLETON, parameters[i], annotations[i]));
        }
        return dependencies;
    }

    private List<ObjectIdentifier> buildFieldDependencies() {
        return fields.stream()
            .map(f -> new ObjectIdentifier(SINGLETON, f.getGenericType(), f.getAnnotations()))
            .collect(Collectors.toList());
    }
}
