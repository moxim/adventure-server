package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.RandomActionData;

class RandomActionEditorTest {

    @Test
    void validate_withNoName_returnsFalse() {
        RandomActionEditor editor = new RandomActionEditor(new RandomActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withANewName_returnsTrue_andDefinesTheVariableInTheAdventure() {
        AdventureData adventure = new AdventureData();
        RandomActionData data = new RandomActionData();
        data.setName("dice");
        RandomActionEditor editor = new RandomActionEditor(data, adventure);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
        assertThat(adventure.getVariableData().getVariables()).containsEntry("dice", 0);
    }

    @Test
    void validate_doesNotOverwriteTheInitialValueOfAnExistingVariable() {
        AdventureData adventure = new AdventureData();
        adventure.getVariableData().getVariables().put("dice", 7);
        RandomActionData data = new RandomActionData();
        data.setName("dice");
        RandomActionEditor editor = new RandomActionEditor(data, adventure);
        editor.initialize();

        editor.validate();

        assertThat(adventure.getVariableData().getVariables()).containsEntry("dice", 7);
    }

    @Test
    void getActionSummary_withAName_isTheName() {
        RandomActionData data = new RandomActionData();
        data.setName("dice");
        RandomActionEditor editor = new RandomActionEditor(data);
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("dice");
    }

    @Test
    void getActionSummary_withoutAName_saysNone() {
        RandomActionEditor editor = new RandomActionEditor(new RandomActionData());
        editor.initialize();

        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }
}
