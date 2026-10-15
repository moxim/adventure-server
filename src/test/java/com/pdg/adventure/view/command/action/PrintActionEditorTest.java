package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.PrintActionData;

class PrintActionEditorTest {

    @Test
    void validate_withNoName_returnsFalse() {
        PrintActionEditor editor = new PrintActionEditor(new PrintActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withAName_returnsTrue() {
        PrintActionData data = new PrintActionData();
        data.setName("VISITED");
        PrintActionEditor editor = new PrintActionEditor(data);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void initialize_buildsTheUI() {
        PrintActionEditor editor = new PrintActionEditor(new PrintActionData());

        editor.initialize();

        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getActionSummary_withAName_isTheName() {
        PrintActionData data = new PrintActionData();
        data.setName("score");
        PrintActionEditor editor = new PrintActionEditor(data);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("score");
    }

    @Test
    void getActionSummary_withoutAName_saysNone() {
        PrintActionEditor editor = new PrintActionEditor(new PrintActionData());
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }
}
