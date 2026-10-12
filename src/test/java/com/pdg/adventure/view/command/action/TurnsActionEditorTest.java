package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.TurnsActionData;

class TurnsActionEditorTest {

    private final TurnsActionData actionData = new TurnsActionData();

    @Test
    void validate_shouldAlwaysReturnTrue() {
        TurnsActionEditor editor = new TurnsActionEditor(actionData);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_shouldSetActionData() {
        assertThat(new TurnsActionEditor(actionData).getActionData()).isSameAs(actionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        TurnsActionEditor editor = new TurnsActionEditor(actionData);

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionTypeName_shouldReturnTurnsActionName() {
        assertThat(new TurnsActionEditor(actionData).getActionTypeName()).isEqualTo(actionData.getActionName());
    }
}
