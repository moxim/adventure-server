package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.AdverbConditionData;

class AdverbConditionEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        VocabularyData vocabulary = new VocabularyData();
        vocabulary.createWord("slowly", Word.Type.ADVERB);
        vocabulary.createWord("quickly", Word.Type.ADVERB);
        adventureData.setVocabularyData(vocabulary);
    }

    @Test
    void validate_withNoAdverbSelected_returnsFalse() {
        AdverbConditionData data = new AdverbConditionData();
        AdverbConditionEditor editor = new AdverbConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void validate_withAdverbPreSelected_returnsTrue() {
        AdverbConditionData data = new AdverbConditionData();
        data.setAdverbText("slowly");
        AdverbConditionEditor editor = new AdverbConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void constructor_setsConditionDataReference() {
        AdverbConditionData data = new AdverbConditionData();
        AdverbConditionEditor editor = new AdverbConditionEditor(data, adventureData);
        assertThat(editor.getConditionData()).isSameAs(data);
    }

    @Test
    void initialize_buildsUI() {
        AdverbConditionEditor editor = new AdverbConditionEditor(new AdverbConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void getConditionSummary_withNoSelection_returnsNone() {
        AdverbConditionEditor editor = new AdverbConditionEditor(new AdverbConditionData(), adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("(none)");
    }

    @Test
    void getConditionSummary_withAdverbPreSelected_returnsItsText() {
        AdverbConditionData data = new AdverbConditionData();
        data.setAdverbText("quickly");
        AdverbConditionEditor editor = new AdverbConditionEditor(data, adventureData);
        editor.initialize();
        assertThat(editor.getConditionSummary()).isEqualTo("quickly");
    }
}
