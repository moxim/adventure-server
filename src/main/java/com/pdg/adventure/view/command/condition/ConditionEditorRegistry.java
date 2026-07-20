package com.pdg.adventure.view.command.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.PreConditionData;

/**
 * Discovers concrete {@link ConditionEditorComponent} implementations annotated with
 * {@link AutoRegisterConditionEditor} via a one-time classpath scan, keyed by the
 * {@link PreConditionData} subtype each one declares as its generic type argument. Replaces the
 * switch statement that used to live in {@link ConditionEditorFactory}: adding a new editor no
 * longer requires touching the factory.
 */
final class ConditionEditorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEditorRegistry.class);
    private static final String BASE_PACKAGE = ConditionEditorRegistry.class.getPackageName();

    private static final Map<Class<? extends PreConditionData>, Class<? extends ConditionEditorComponent<?>>> EDITORS_BY_DATA_TYPE =
            scan();

    private ConditionEditorRegistry() {
    }

    static Class<? extends ConditionEditorComponent<?>> editorClassFor(Class<? extends PreConditionData> dataClass) {
        return EDITORS_BY_DATA_TYPE.get(dataClass);
    }

    static ConditionEditorComponent<?> instantiate(Class<? extends ConditionEditorComponent<?>> editorClass,
                                                    PreConditionData conditionData, AdventureData adventureData) {
        try {
            Constructor<?> twoArg = findConstructor(editorClass, conditionData.getClass(), AdventureData.class);
            if (twoArg != null) {
                return (ConditionEditorComponent<?>) twoArg.newInstance(conditionData, adventureData);
            }
            Constructor<?> oneArg = findConstructor(editorClass, conditionData.getClass());
            if (oneArg != null) {
                return (ConditionEditorComponent<?>) oneArg.newInstance(conditionData);
            }
            throw new IllegalStateException(
                    "No (" + conditionData.getClass().getSimpleName() + ") or (" + conditionData.getClass().getSimpleName()
                    + ", AdventureData) constructor found on " + editorClass.getName());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate condition editor " + editorClass.getName(), e);
        }
    }

    private static Constructor<?> findConstructor(Class<?> type, Class<?>... paramTypes) {
        try {
            return type.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends PreConditionData>, Class<? extends ConditionEditorComponent<?>>> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AutoRegisterConditionEditor.class));

        Map<Class<? extends PreConditionData>, Class<? extends ConditionEditorComponent<?>>> result = new HashMap<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> editorClass = resolveClass(candidate.getBeanClassName());
            if (!ConditionEditorComponent.class.isAssignableFrom(editorClass)) {
                logger.warn("{} is annotated with @AutoRegisterConditionEditor but does not extend "
                            + "ConditionEditorComponent", editorClass.getName());
                continue;
            }
            Class<?> dataClass = GenericTypeResolver.resolveTypeArgument(editorClass, ConditionEditorComponent.class);
            if (dataClass == null) {
                logger.warn("Could not resolve the PreConditionData type for {}; it must extend "
                            + "ConditionEditorComponent<SomeConditionData> (directly or via an abstract base)",
                            editorClass.getName());
                continue;
            }
            result.put((Class<? extends PreConditionData>) dataClass, (Class<? extends ConditionEditorComponent<?>>) editorClass);
        }
        logger.info("Auto-registered {} condition editors", result.size());
        return Collections.unmodifiableMap(result);
    }

    private static Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load scanned condition editor class " + className, e);
        }
    }
}
