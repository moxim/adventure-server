package com.pdg.adventure.view.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.select.Select;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds the first select box of an editor and brings the author's attention to it, so a freshly added action or
 * condition can be filled in right away.
 */
public final class SelectBoxFocus {

    private SelectBoxFocus() {
    }

    /**
     * Puts the cursor into the first select box ({@link ComboBox} or {@link Select}) below the given component and
     * scrolls it into view. Does nothing if there is none (an editor that takes no arguments).
     */
    public static void focusFirstIn(Component aParent) {
        firstIn(aParent).ifPresent(selectBox -> {
            ((Focusable<?>) selectBox).focus();
            selectBox.scrollIntoView();
        });
    }

    /** The first select box below the given component, in display order. */
    public static Optional<Component> firstIn(Component aParent) {
        return descendantsOf(aParent).filter(child -> child instanceof ComboBox<?> || child instanceof Select<?>)
                                     .findFirst();
    }

    /** All components below the given one, parents before their children, in display order. */
    private static Stream<Component> descendantsOf(Component aParent) {
        return aParent.getChildren().flatMap(child -> Stream.concat(Stream.of(child), descendantsOf(child)));
    }
}
