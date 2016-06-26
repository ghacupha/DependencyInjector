package ch.jalu.injector.testing.runner;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Statement for initializing {@link InjectDelayed} fields. These fields are
 * constructed after {@link BeforeInjecting} and before JUnit's &#064;Before.
 */
public class RunDelayedInjects extends Statement {

    private final Statement next;
    private TestClass testClass;
    private Object target;
    private List<FrameworkField> fields;

    public RunDelayedInjects(Statement next, TestClass testClass, Object target, List<FrameworkField> fields) {
        this.next = next;
        this.testClass = testClass;
        this.target = target;
        this.fields = fields;
    }

    @Override
    public void evaluate() throws Throwable {
        Injector injector = getInjector();
        for (FrameworkField frameworkField : fields) {
            Field field = frameworkField.getField();
            if (ReflectionUtils.getFieldValue(field, target) != null) {
                throw new IllegalStateException("Field with @InjectDelayed must be null on startup. "
                    + "Field '" + field.getName() + "' is not null");
            }
            Object object = injector.getSingleton(field.getType());
            ReflectionUtils.setField(field, target, object);
        }

        this.testClass = null;
        this.target = null;
        this.fields = null;
        next.evaluate();
    }

    protected Injector getInjector() {
        InjectorBuilder injectorBuilder = new InjectorBuilder();
        List<InstantiationProvider> instantiationProviders = injectorBuilder.createInstantiationProviders();
        return injectorBuilder.addHandlers(instantiationProviders)
            .addHandlers(new MockDependencyHandler(testClass, target))
            .addHandlers(new PostConstructMethodInvoker())
            .create();
    }
}
