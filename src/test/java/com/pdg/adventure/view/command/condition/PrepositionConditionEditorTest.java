package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.PrepositionConditionData;

class PrepositionConditionEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("on", Word.Type.PREPOSITION);
        vocabulary.createWord("off", Word.Type.PREPOSITION);
        adventureData.setVocabularyData(vocabulary);
    }

    @Test
    void validate_withNoPrepositionSelected_returnsFalse() {
        PrepositionConditionData data = new PrepositionConditionData();
        PrepositionConditionEditor editor = new PrepositionConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withPrepositionPreSelected_returnsTrue() {
        PrepositionConditionData data = new PrepositionConditionData();
        data.setPrepositionText("on");
        PrepositionConditionEditor editor = new PrepositionConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        PrepositionConditionData data = new PrepositionConditionData();
        PrepositionConditionEditor editor = new PrepositionConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        PrepositionConditionEditor editor =
                new PrepositionConditionEditor(new PrepositionConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        PrepositionConditionEditor editor =
                new PrepositionConditionEditor(new PrepositionConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withPrepositionPreSelected_returnsItsText() {
        PrepositionConditionData data = new PrepositionConditionData();
        data.setPrepositionText("off");
        PrepositionConditionEditor editor = new PrepositionConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("off");
    }
}
