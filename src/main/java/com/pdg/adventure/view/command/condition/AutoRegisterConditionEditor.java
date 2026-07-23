package com.pdg.adventure.view.command.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a concrete {@link ConditionEditorComponent} implementation for automatic discovery by
 * {@link ConditionEditorFactory}. The condition type it edits is detected from its generic type
 * argument (directly, or through an abstract base such as {@link AbstractSingleItemConditionEditor}
 * or {@link AbstractNumericComparisonConditionEditor}) - no manual registration needed.
 * <p>
 * The class must have a {@code public} constructor taking either just its condition data type, or
 * (condition data type, {@link com.pdg.adventure.model.AdventureData}) - the same two shapes
 * already used across the existing editors.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterConditionEditor {
}
