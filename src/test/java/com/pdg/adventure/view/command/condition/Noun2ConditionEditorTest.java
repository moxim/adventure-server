package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.Noun2ConditionData;

class Noun2ConditionEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("machine", Word.Type.NOUN);
        vocabulary.createWord("engine", Word.Type.NOUN);
        adventureData.setVocabularyData(vocabulary);
    }

    @Test
    void validate_withNoNoun2Selected_returnsFalse() {
        Noun2ConditionData data = new Noun2ConditionData();
        Noun2ConditionEditor editor = new Noun2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withNoun2PreSelected_returnsTrue() {
        Noun2ConditionData data = new Noun2ConditionData();
        data.setNoun2Text("machine");
        Noun2ConditionEditor editor = new Noun2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        Noun2ConditionData data = new Noun2ConditionData();
        Noun2ConditionEditor editor = new Noun2ConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        Noun2ConditionEditor editor = new Noun2ConditionEditor(new Noun2ConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        Noun2ConditionEditor editor = new Noun2ConditionEditor(new Noun2ConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withNoun2PreSelected_returnsItsText() {
        Noun2ConditionData data = new Noun2ConditionData();
        data.setNoun2Text("engine");
        Noun2ConditionEditor editor = new Noun2ConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("engine");
    }
}
