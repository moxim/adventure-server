package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

/**
 * Base of the editors for the actions that apply one variable to another (Add, Subtract): the author picks the first
 * variable, whose value is applied, and the second, which changes. Both must be defined variables.
 */
abstract class AbstractTwoVariablesActionEditor<D extends ActionData> extends ActionEditorComponent<D> {
    private final AdventureData adventureData;
    private VariableNameSelector firstField;
    private VariableNameSelector secondField;

    AbstractTwoVariablesActionEditor(D anActionData, AdventureData anAdventureData) {
        super(anActionData);
        adventureData = anAdventureData;
    }

    abstract String title();

    abstract String descriptionText();

    /** The operator shown in the summary, e.g. {@code +=}: the second variable &lt;operator&gt; the first. */
    abstract String operator();

    abstract String firstName();

    abstract String secondName();

    abstract void setFirstName(String aName);

    abstract void setSecondName(String aName);

    @Override
    protected void buildUI() {
        H4 title = new H4(title());
        Span description = new Span(descriptionText());
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        firstField = selector("First Variable", firstName(), "Its value is applied; it does not change.");
        secondField = selector("Second Variable", secondName(), "This is the variable that changes.");

        firstField.addValueChangeListener(e -> setFirstName(e.getValue()));
        secondField.addValueChangeListener(e -> setSecondName(e.getValue()));

        add(title, description, firstField, secondField);
    }

    private VariableNameSelector selector(String aLabel, String aCurrentName, String aHelperText) {
        VariableNameSelector field = new VariableNameSelector(aLabel,
                adventureData == null ? null : () -> VariableChoices.of(adventureData), false, aCurrentName);
        field.setPlaceholder("Select a variable");
        field.setHelperText(aHelperText + " Only defined variables can be chosen - a Set Variable action defines one.");
        return field;
    }

    @Override
    public boolean validate() {
        // Both fields are validated, so that both show their message.
        boolean firstValid = firstField.validateSelection();
        boolean secondValid = secondField.validateSelection();
        return firstValid && secondValid;
    }

    @Override
    public String getActionSummary() {
        String first = firstField == null ? "" : firstField.selectedName();
        String second = secondField == null ? "" : secondField.selectedName();
        if (first.isEmpty() || second.isEmpty()) return "(none)";
        return second + " " + operator() + " " + first;
    }
}
