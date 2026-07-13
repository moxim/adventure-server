package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.action.SetVariableActionData;

class ActionListEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setLocationData(new HashMap<>());
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void editingLeafField_updatesRowSummaryLive() {
        ActionListEditor editor = new ActionListEditor(adventureData);
        editor.setActions(List.of(new SetVariableActionData(null, null)));
        ActionRow row = rowsOf(editor).getFirst();
        assertThat(row.getSummaryText()).isEqualTo("SetVariable: (none)");

        // Simulate the user typing into the editor's fields — no manual refresh.
        List<TextField> fields = textFieldsOf(row);
        fields.get(0).setValue("score");
        fields.get(1).setValue("10");

        assertThat(row.getSummaryText()).isEqualTo("SetVariable: score = 10");
    }

    private List<ActionRow> rowsOf(ActionListEditor editor) {
        return editor.getChildren()
                .filter(c -> c instanceof VerticalLayout)
                .findFirst()
                .map(rowsLayout -> rowsLayout.getChildren()
                        .filter(c -> c instanceof ActionRow)
                        .map(c -> (ActionRow) c)
                        .toList())
                .orElseThrow();
    }

    // ActionRow (Details) -> Div -> ActionEditorComponent -> TextField fields
    private List<TextField> textFieldsOf(ActionRow row) {
        ActionEditorComponent editor = row.getChildren()
                .filter(c -> c instanceof Div).findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof ActionEditorComponent).findFirst())
                .map(c -> (ActionEditorComponent) c)
                .orElseThrow();
        return editor.getChildren()
                .filter(c -> c instanceof TextField)
                .map(c -> (TextField) c)
                .toList();
    }
}
