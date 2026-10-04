package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.Map;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.SetVariableActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

@AutoRegisterActionEditor
public class SetVariableActionEditor extends ActionEditorComponent<SetVariableActionData> {
    private final SetVariableActionData setVariableActionData;
    private VariableNameSelector variableNameField;
    private final AdventureData adventureData;
    private IntegerField variableValueField;

    public SetVariableActionEditor(SetVariableActionData anActionData, AdventureData anAdventureData) {
        super(anActionData);
        setVariableActionData = anActionData;
        adventureData = anAdventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Set Variable Action");
        Span description = new Span("Set a named variable to a specific value");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), true,
                setVariableActionData.getVariableName());
        variableNameField.setPlaceholder("Select a variable or type a new name");
        variableNameField.setHelperText("This action defines the variable; it is then available " +
                                        "in conditions and in Increment/Decrement.");

        variableValueField = new IntegerField("Variable Value");
        variableValueField.setPlaceholder("Enter variable value");
        variableValueField.setWidthFull();
        variableValueField.setRequired(true);

        if (setVariableActionData.getVariableValue() != null) {
            variableValueField.setValue(setVariableActionData.getVariableValue());
        }

        variableNameField.addValueChangeListener(e -> {
            setVariableActionData.setVariableName(e.getValue());
            // Define the variable straight away so conditions can pick it before this command is saved.
            if (adventureData != null) {
                defineVariable(e.getValue(), variableValueField.getValue());
            }
        });
        variableValueField.addValueChangeListener(e ->
                                                          setVariableActionData.setVariableValue(e.getValue()));

        add(title, description, variableNameField, variableValueField);
    }

    @Override
    public boolean validate() {
        boolean nameValid = variableNameField.validateSelection();
        boolean valueValid = variableValueField.getValue() != null;

        if (!valueValid) {
            variableValueField.setErrorMessage("Please enter a variable value");
            variableValueField.setInvalid(true);
        } else {
            variableValueField.setInvalid(false);
            if (nameValid && adventureData != null) {
                defineVariable(variableNameField.selectedName(), variableValueField.getValue());
            }
        }

        return nameValid && valueValid;
    }

    @Override
    public String getActionSummary() {
        String name = (variableNameField != null && !variableNameField.selectedName().isEmpty())
                ? variableNameField.selectedName() : "";
        String value = (variableValueField != null && variableValueField.getValue() != null)
                ? variableValueField.getValue().toString() : "";
        if (name.isEmpty()) return "(none)";
        return name + " = " + value;
    }

    /**
     * Registers a variable under the given name with a value. Called as soon
     * as an author names a variable in a Set Variable action, so it is available to conditions
     * straight away; it is persisted with the next save of the adventure.
     *
     * @return true if the variable was newly defined
     */
    private boolean defineVariable(String aName, final Integer aValue) {
        if (aName == null || aName.isBlank()) {
            return false;
        }
        String name = aName.trim();
        final Map<String, Integer> variables = adventureData.getVariableData().getVariables();
        variables.put(name, aValue);
        return true;
    }
}
