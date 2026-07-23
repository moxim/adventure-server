package com.pdg.adventure.view.command.action;

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
import com.pdg.adventure.model.action.ActionData;

/**
 * Discovers concrete {@link ActionEditorComponent} implementations annotated with
 * {@link AutoRegisterActionEditor} via a one-time classpath scan, keyed by the {@link ActionData}
 * subtype each one declares as its generic type argument. Replaces the switch statement that used
 * to live in {@link ActionEditorFactory}: adding a new editor no longer requires touching the factory.
 */
final class ActionEditorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ActionEditorRegistry.class);
    private static final String BASE_PACKAGE = ActionEditorRegistry.class.getPackageName();

    private static final Map<Class<? extends ActionData>, Class<? extends ActionEditorComponent<?>>> EDITORS_BY_DATA_TYPE =
            scan();

    private ActionEditorRegistry() {
    }

    static Class<? extends ActionEditorComponent<?>> editorClassFor(Class<? extends ActionData> dataClass) {
        return EDITORS_BY_DATA_TYPE.get(dataClass);
    }

    static ActionEditorComponent<?> instantiate(Class<? extends ActionEditorComponent<?>> editorClass,
                                                ActionData actionData, AdventureData adventureData) {
        try {
            Constructor<?> twoArg = findConstructor(editorClass, actionData.getClass(), AdventureData.class);
            if (twoArg != null) {
                return (ActionEditorComponent<?>) twoArg.newInstance(actionData, adventureData);
            }
            Constructor<?> oneArg = findConstructor(editorClass, actionData.getClass());
            if (oneArg != null) {
                return (ActionEditorComponent<?>) oneArg.newInstance(actionData);
            }
            throw new IllegalStateException(
                    "No (" + actionData.getClass().getSimpleName() + ") or (" + actionData.getClass().getSimpleName()
                    + ", AdventureData) constructor found on " + editorClass.getName());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate action editor " + editorClass.getName(), e);
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
    private static Map<Class<? extends ActionData>, Class<? extends ActionEditorComponent<?>>> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AutoRegisterActionEditor.class));

        Map<Class<? extends ActionData>, Class<? extends ActionEditorComponent<?>>> result = new HashMap<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> editorClass = resolveClass(candidate.getBeanClassName());
            if (!ActionEditorComponent.class.isAssignableFrom(editorClass)) {
                logger.warn("{} is annotated with @AutoRegisterActionEditor but does not extend ActionEditorComponent",
                            editorClass.getName());
                continue;
            }
            Class<?> dataClass = GenericTypeResolver.resolveTypeArgument(editorClass, ActionEditorComponent.class);
            if (dataClass == null) {
                logger.warn("Could not resolve the ActionData type for {}; it must extend "
                            + "ActionEditorComponent<SomeActionData> (directly or via an abstract base)",
                            editorClass.getName());
                continue;
            }
            result.put((Class<? extends ActionData>) dataClass, (Class<? extends ActionEditorComponent<?>>) editorClass);
        }
        logger.info("Auto-registered {} action editors", result.size());
        return Collections.unmodifiableMap(result);
    }

    private static Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load scanned action editor class " + className, e);
        }
    }
}
