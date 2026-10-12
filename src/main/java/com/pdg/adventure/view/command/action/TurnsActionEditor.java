package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.TurnsActionData;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * Editor component for TurnsActionData.
 * The turns action has no configurable parameters: it prints the value of the standard {@code TURNS} variable
 * in the wording of system messages SM17 to SM20.
 */
@AutoRegisterActionEditor
public class TurnsActionEditor extends ActionEditorComponent<TurnsActionData> {

    public TurnsActionEditor(TurnsActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Turns Action");

        Span description = new Span("Tell the player how many turns they have taken, for example \"You have taken 5 turns.\" "
                                    + "This action takes no parameters.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ The number is the standard variable " + VariableProvider.TURNS_VARIABLE_NAME
                             + ", which counts every command the game understands. The wording can be changed under "
                             + "Manage System Messages (SM17 to SM20).");

        add(title, description, info);
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
