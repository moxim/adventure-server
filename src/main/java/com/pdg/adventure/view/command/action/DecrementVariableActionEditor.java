package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for DecrementVariableActionData.
 * Allows specifying the variable name and the amount to decrement it by.
 */
@AutoRegisterActionEditor
public class DecrementVariableActionEditor extends ActionEditorComponent<DecrementVariableActionData> {
    private final DecrementVariableActionData decrementVariableActionData;
    private VariableNameSelector nameField;
    private final AdventureData adventureData;
    private IntegerField valueField;

    public DecrementVariableActionEditor(DecrementVariableActionData aDecrementVariableActionData) {
        this(aDecrementVariableActionData, null);
    }

    public DecrementVariableActionEditor(DecrementVariableActionData aDecrementVariableActionData,
                                         AdventureData anAdventureData) {
        super(aDecrementVariableActionData);
        decrementVariableActionData = aDecrementVariableActionData;
        adventureData = anAdventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Decrement Variable Action");
        Span description = new Span("Decrement a named variable by the specified amount");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        nameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false,
                decrementVariableActionData.getName());
        nameField.setPlaceholder("Select a variable");
        nameField.setHelperText("Only defined variables can be chosen - a Set Variable action defines one.");

        valueField = new IntegerField("Decrement Amount");
        valueField.setPlaceholder("Enter decrement amount");
        valueField.setWidthFull();
        valueField.setRequired(true);

        // Pre-populate fields if actionData already has values
        if (decrementVariableActionData.getValue() != null) {
            valueField.setValue(decrementVariableActionData.getValue());
        }

        // Write back to actionData on change
        nameField.addValueChangeListener(e -> decrementVariableActionData.setName(e.getValue()));
        valueField.addValueChangeListener(e -> decrementVariableActionData.setValue(e.getValue()));

        add(title, description, nameField, valueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = nameField.validateSelection();
        boolean valueValid = valueField.getValue() != null;

        valueField.setInvalid(!valueValid);
        if (!valueValid) {
            valueField.setErrorMessage("Please enter a decrement amount");
        }

        return nameValid && valueValid;
    }

    @Override
    public String getActionSummary() {
        String name = (nameField != null && !nameField.selectedName().isEmpty())
                ? nameField.selectedName() : "";
        String amount = (valueField != null && valueField.getValue() != null)
                ? valueField.getValue().toString() : "";
        if (name.isEmpty()) return "(none)";
        return name + " -= " + amount;
    }
}
