package com.pdg.adventure.view.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VariableNameSelectorTest {

    @Test
    void restricted_acceptsOnlyDefinedNames_andFlagsAStoredUndefinedName() {
        VariableNameSelector selector =
                new VariableNameSelector("Variable", () -> List.of("intoxication", "score"), false, "intoxication");
        assertThat(selector.isAcceptable()).isTrue();

        VariableNameSelector stale = new VariableNameSelector("Variable", () -> List.of("score"), false, "gone");
        assertThat(stale.selectedName()).isEqualTo("gone");
        assertThat(stale.validateSelection()).isFalse();
        assertThat(stale.isInvalid()).isTrue();
        assertThat(stale.getErrorMessage()).contains("gone").contains("Set Variable");
    }

    @Test
    void restricted_withNothingSelected_isNotAcceptable() {
        VariableNameSelector selector = new VariableNameSelector("Variable", () -> List.of("score"), false, null);

        assertThat(selector.validateSelection()).isFalse();
        assertThat(selector.getErrorMessage()).isEqualTo("Please select a variable");
    }

    @Test
    void allowNew_acceptsAnyNonBlankName() {
        VariableNameSelector selector = new VariableNameSelector("Variable", () -> List.of("score"), true, "brandNew");

        assertThat(selector.isAcceptable()).isTrue();
        assertThat(selector.isAllowCustomValue()).isTrue();
    }

    @Test
    void focus_refreshesTheItemsFromTheSupplier_soNewlyDefinedNamesAppear() {
        java.util.List<String> names = new java.util.ArrayList<>(List.of("score"));
        VariableNameSelector selector = new VariableNameSelector("Variable", () -> names, false, null);
        assertThat(selector.getListDataView().getItems()).containsExactly("score");

        names.add("Intoxication");
        com.vaadin.flow.component.ComponentUtil.fireEvent(selector,
                new com.vaadin.flow.component.FocusNotifier.FocusEvent<>(selector, true));

        assertThat(selector.getListDataView().getItems()).containsExactly("score", "Intoxication");
        assertThat(selector.isAcceptable()).isFalse(); // nothing selected yet
    }

    @Test
    void withoutAdventureContext_theChoiceIsUnrestricted() {
        VariableNameSelector selector = new VariableNameSelector("Variable", null, false, "anything");

        assertThat(selector.isAcceptable()).isTrue();
    }

    @Test
    void restricted_doesNotAllowTypingACustomValue() {
        assertThat(new VariableNameSelector("Variable", () -> List.of("score"), false, null).isAllowCustomValue()).isFalse();
    }
}
