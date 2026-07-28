package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.BreakActionData;

class BreakActionEditorTest {

    @Test
    void constructor_shouldSetActionData() {
        BreakActionData actionData = new BreakActionData();

        BreakActionEditor editor = new BreakActionEditor(actionData);

        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    @Test
    void validate_shouldAlwaysReturnTrue() {
        BreakActionData actionData = new BreakActionData();
        BreakActionEditor editor = new BreakActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_shouldBuildUIChildren() {
        BreakActionData actionData = new BreakActionData();
        BreakActionEditor editor = new BreakActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnBreakActionName() {
        BreakActionData actionData = new BreakActionData();
        BreakActionEditor editor = new BreakActionEditor(actionData);

        assertThat(editor.getActionTypeName()).isEqualTo(actionData.getActionName());
    }

    @Test
    void getActionSummary_returnsEmpty() {
        BreakActionData actionData = new BreakActionData();
        BreakActionEditor editor = new BreakActionEditor(actionData);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEmpty();
    }
}
