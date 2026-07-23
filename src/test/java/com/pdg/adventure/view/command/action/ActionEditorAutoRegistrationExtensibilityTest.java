package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;

/**
 * Proves the actual point of the annotation-driven registry: a brand new action editor, added and
 * annotated with @AutoRegisterActionEditor, is discovered automatically with no change anywhere
 * else - especially not to ActionEditorFactory itself.
 */
class ActionEditorAutoRegistrationExtensibilityTest {

    @Test
    void newlyAddedAnnotatedEditor_isDiscoveredWithoutTouchingTheFactory() {
        AdventureData adventureData = new AdventureData();
        FreshlyAddedActionData actionData = new FreshlyAddedActionData();

        ActionEditorComponent editor = ActionEditorFactory.createEditor(actionData, adventureData);

        assertThat(editor).isInstanceOf(FreshlyAddedActionEditor.class);
        assertThat(editor.getActionData()).isSameAs(actionData);
    }

    static class FreshlyAddedActionData extends ActionData {
    }

    @AutoRegisterActionEditor
    static class FreshlyAddedActionEditor extends ActionEditorComponent<FreshlyAddedActionData> {

        public FreshlyAddedActionEditor(FreshlyAddedActionData actionData) {
            super(actionData);
        }

        @Override
        protected void buildUI() {
        }

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public String getActionSummary() {
            return "";
        }
    }
}
