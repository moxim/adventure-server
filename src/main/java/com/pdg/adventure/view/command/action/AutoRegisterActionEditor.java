package com.pdg.adventure.view.command.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a concrete {@link ActionEditorComponent} implementation for automatic discovery by
 * {@link ActionEditorFactory}. The action type it edits is detected from its generic type
 * argument (directly, or through an abstract base such as {@link AbstractSingleItemActionEditor}) -
 * no manual registration needed.
 * <p>
 * The class must have a {@code public} constructor taking either just its action data type, or
 * (action data type, {@link com.pdg.adventure.model.AdventureData}) - the same two shapes already
 * used across the existing editors.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterActionEditor {
}
