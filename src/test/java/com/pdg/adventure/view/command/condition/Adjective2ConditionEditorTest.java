package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.Adjective2ConditionData;

class Adjective2ConditionEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("ancient", Word.Type.ADJECTIVE);
        vocabulary.createWord("rusty", Word.Type.ADJECTIVE);
        adventureData.setVocabularyData(vocabulary);
    }

    @Test
    void validate_withNoAdjective2Selected_returnsFalse() {
        Adjective2ConditionData data = new Adjective2ConditionData();
        Adjective2ConditionEditor editor = new Adjective2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withAdjective2PreSelected_returnsTrue() {
        Adjective2ConditionData data = new Adjective2ConditionData();
        data.setAdjective2Text("ancient");
        Adjective2ConditionEditor editor = new Adjective2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        Adjective2ConditionData data = new Adjective2ConditionData();
        Adjective2ConditionEditor editor = new Adjective2ConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        Adjective2ConditionEditor editor =
                new Adjective2ConditionEditor(new Adjective2ConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        Adjective2ConditionEditor editor =
                new Adjective2ConditionEditor(new Adjective2ConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withAdjective2PreSelected_returnsItsText() {
        Adjective2ConditionData data = new Adjective2ConditionData();
        data.setAdjective2Text("rusty");
        Adjective2ConditionEditor editor = new Adjective2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("rusty");
    }
}
