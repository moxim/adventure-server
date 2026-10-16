package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;

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
        List<VariableNameSelector> textFields = textFieldsOf(row);
        textFields.getFirst().setValue("score");

        List<IntegerField> integerFields = integerFieldsOf(row);
        integerFields.getFirst().setValue(10);

        assertThat(row.getSummaryText()).isEqualTo("SetVariable: score = 10");
    }

    @Test
    void addingAnActionFromTheSelector_focusesItsFirstSelectBoxAndScrollsItIntoView() {
        UI ui = attachedUi();
        ActionListEditor editor = new ActionListEditor(adventureData);
        ui.add(editor);

        chooseAndAdd(editor, "Add Variable");

        ActionRow row = rowsOf(editor).getFirst();
        VariableNameSelector first = textFieldsOf(row).getFirst();
        assertThat(pendingCallsFor(ui, first)).anyMatch(call -> call.contains("focus"))
                                              .anyMatch(call -> call.contains("scrollIntoView"));
    }

    @Test
    void loadingExistingActions_doesNotMoveTheFocus() {
        UI ui = attachedUi();
        ActionListEditor editor = new ActionListEditor(adventureData);
        ui.add(editor);

        editor.setActions(List.of(new SetVariableActionData(null, null)));

        VariableNameSelector first = textFieldsOf(rowsOf(editor).getFirst()).getFirst();
        assertThat(pendingCallsFor(ui, first)).noneMatch(call -> call.contains("focus") || call.contains("scrollIntoView"));
    }

    @Test
    void addingAnActionWithoutSelectBox_doesNotFail() {
        UI ui = attachedUi();
        ActionListEditor editor = new ActionListEditor(adventureData);
        ui.add(editor);

        chooseAndAdd(editor, "Quit");

        assertThat(rowsOf(editor)).hasSize(1);
    }

    @Test
    void validate_marksTheInvalidFieldsOfEveryAction_notOnlyOfTheFirstInvalidOne() {
        ActionListEditor editor = new ActionListEditor(adventureData);
        editor.setActions(List.of(new SetVariableActionData(null, null), new SetVariableActionData(null, null)));

        boolean valid = editor.validate();

        assertThat(valid).isFalse();
        assertThat(rowsOf(editor)).allSatisfy(row -> assertThat(textFieldsOf(row).getFirst().isInvalid()).isTrue());
    }

    @Test
    void addingAnAction_doesNotMarkItsEmptyFieldsInvalid() {
        UI ui = attachedUi();
        ActionListEditor editor = new ActionListEditor(adventureData);
        ui.add(editor);

        chooseAndAdd(editor, "Set Variable");

        assertThat(textFieldsOf(rowsOf(editor).getFirst()).getFirst().isInvalid()).isFalse();
    }

    private static UI attachedUi() {
        UI ui = new UI();
        VaadinSession session = mock(VaadinSession.class);
        when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        UI.setCurrent(ui);
        return ui;
    }

    @SuppressWarnings("unchecked")
    private static void chooseAndAdd(ActionListEditor editor, String actionLabel) {
        ActionSelector selector = editor.getChildren().filter(ActionSelector.class::isInstance)
                .map(ActionSelector.class::cast).findFirst().orElseThrow();
        ComboBox<Object> typeSelector = (ComboBox<Object>) selector.getChildren()
                .filter(ComboBox.class::isInstance).findFirst().orElseThrow();
        Button add = (Button) selector.getChildren().filter(Button.class::isInstance).findFirst().orElseThrow();
        Object type = typeSelector.getListDataView().getItems()
                .filter(item -> actionLabel.equals(typeSelector.getItemLabelGenerator().apply(item)))
                .findFirst().orElseThrow();
        typeSelector.setValue(type);
        ComponentUtil.fireEvent(add, new ClickEvent<>(add));
    }

    // focus() and scrollIntoView() reach the browser as pending JavaScript invocations; reading them clears the queue.
    private static List<String> pendingCallsFor(UI ui, com.vaadin.flow.component.Component component) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(pending -> pending.getInvocation())
                .filter(invocation -> invocation.getParameters().contains(component.getElement()))
                .map(invocation -> invocation.getExpression())
                .toList();
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

    // ActionRow (Details) -> Div -> ActionEditorComponent -> variable-name dropdown
    private List<VariableNameSelector> textFieldsOf(ActionRow row) {
        ActionEditorComponent editor = row.getChildren()
                .filter(c -> c instanceof Div).findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof ActionEditorComponent).findFirst())
                .map(c -> (ActionEditorComponent) c)
                .orElseThrow();
        return editor.getChildren()
                .filter(c -> c instanceof VariableNameSelector)
                .map(c -> (VariableNameSelector) c)
                .toList();
    }

    private List<IntegerField> integerFieldsOf(ActionRow row) {
        ActionEditorComponent editor = row.getChildren()
                .filter(c -> c instanceof Div).findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof ActionEditorComponent).findFirst())
                .map(c -> (ActionEditorComponent) c)
                .orElseThrow();
        return editor.getChildren()
                .filter(c -> c instanceof IntegerField)
                .map(c -> (IntegerField) c)
                .toList();
    }
}
