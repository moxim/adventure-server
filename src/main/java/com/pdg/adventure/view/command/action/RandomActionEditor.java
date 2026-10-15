package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.RandomActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Editor component for RandomActionData.
 * Allows choosing the variable the game sets to a random number from 0 to 99. Like Set Variable, it may name a
 * new variable, which is defined in the adventure straight away so conditions can pick it.
 */
@AutoRegisterActionEditor
public class RandomActionEditor extends ActionEditorComponent<RandomActionData> {
    private final RandomActionData randomActionData;
    private final AdventureData adventureData;
    private VariableNameSelector variableNameField;

    public RandomActionEditor(RandomActionData actionData) {
        this(actionData, null);
    }

    public RandomActionEditor(RandomActionData actionData, AdventureData anAdventureData) {
        super(actionData);
        this.randomActionData = actionData;
        this.adventureData = anAdventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Random Action");
        Span description = new Span("Set a variable to a random whole number from 0 to 99");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        variableNameField = new VariableNameSelector("Variable Name",
                adventureData == null ? null : () -> VariableChoices.of(adventureData), true,
                randomActionData.getName());
        variableNameField.setPlaceholder("Select a variable or type a new name");
        variableNameField.setHelperText("A name that does not exist yet creates the variable.");

        variableNameField.addValueChangeListener(e -> {
            randomActionData.setName(e.getValue());
            defineVariable(e.getValue());
        });

        add(title, description, variableNameField);
    }

    @Override
    public boolean validate() {
        boolean valid = variableNameField.validateSelection();
        if (valid) {
            defineVariable(variableNameField.selectedName());
        }
        return valid;
    }

    @Override
    public String getActionSummary() {
        String name = variableNameField == null ? "" : variableNameField.selectedName();
        return name.isEmpty() ? "(none)" : name;
    }

    /** Registers the variable (at 0, if it is new) so conditions can pick it before this command is saved. */
    private void defineVariable(String aName) {
        if (adventureData == null || aName == null || aName.isBlank()) {
            return;
        }
        adventureData.getVariableData().getVariables().putIfAbsent(aName.trim(), 0);
    }
}
