package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.Component;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.*;
import com.pdg.adventure.view.command.VariableNameSelector;

class FirstSelectBoxTest {

    private final AdventureData adventureData = new AdventureData();

    private ActionEditorComponent<ActionData> editorFor(ActionData data) {
        return ActionEditorFactory.createEditor(data, adventureData);
    }

    @Test
    void editorWithOneVariableSelector_hasItAsFirstSelectBox() {
        ActionEditorComponent<ActionData> editor = editorFor(new SetVariableActionData(null, null));

        assertThat(editor.firstSelectBox()).containsInstanceOf(VariableNameSelector.class);
    }

    @Test
    void editorWithTwoVariableSelectors_hasTheFirstVariableAsFirstSelectBox() {
        ActionEditorComponent<ActionData> editor = editorFor(new AddVariableActionData());

        Component first = editor.firstSelectBox().orElseThrow();

        assertThat(((VariableNameSelector) first).getLabel()).isEqualTo("First Variable");
    }

    @Test
    void editorsWithAnItemOrMessageChoice_haveAFirstSelectBox() {
        assertThat(editorFor(new TakeActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new MessageActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new MesActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new RandomActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new SysMesActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new PrintActionData()).firstSelectBox()).isPresent();
        assertThat(editorFor(new LightActionData()).firstSelectBox()).isPresent();
    }

    @Test
    void editorsWithoutArguments_haveNoFirstSelectBox() {
        assertThat(editorFor(new QuitActionData()).firstSelectBox()).isEmpty();
        assertThat(editorFor(new InventoryActionData()).firstSelectBox()).isEmpty();
        assertThat(editorFor(new TurnsActionData()).firstSelectBox()).isEmpty();
        assertThat(editorFor(new NewLineActionData()).firstSelectBox()).isEmpty();
    }

    @Test
    void focusFirstSelectBox_withoutSelectBox_doesNothing() {
        editorFor(new QuitActionData()).focusFirstSelectBox();
    }
}
