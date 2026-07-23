package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.PreConditionData;

/**
 * Proves the actual point of the annotation-driven registry: a brand new condition editor, added
 * and annotated with @AutoRegisterConditionEditor, is discovered automatically with no change
 * anywhere else - especially not to ConditionEditorFactory itself.
 */
class ConditionEditorAutoRegistrationExtensibilityTest {

    @Test
    void newlyAddedAnnotatedEditor_isDiscoveredWithoutTouchingTheFactory() {
        AdventureData adventureData = new AdventureData();
        FreshlyAddedConditionData conditionData = new FreshlyAddedConditionData();

        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(conditionData, adventureData);

        assertThat(editor).isInstanceOf(FreshlyAddedConditionEditor.class);
        assertThat(editor.getConditionData()).isSameAs(conditionData);
    }

    static class FreshlyAddedConditionData extends PreConditionData {
    }

    @AutoRegisterConditionEditor
    static class FreshlyAddedConditionEditor extends ConditionEditorComponent<FreshlyAddedConditionData> {

        public FreshlyAddedConditionEditor(FreshlyAddedConditionData conditionData) {
            super(conditionData);
        }

        @Override
        protected void buildUI() {
        }

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public String getConditionSummary() {
            return "";
        }
    }
}
