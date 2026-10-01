package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.AutoTakeActionData;

/**
 * Editor component for AutoTakeActionData (AUTOT).
 * The action has no configurable parameters: it acts on whichever item the player named, so it is
 * meant for a response keyed on the wildcard noun (~), e.g. "TAKE ~".
 */
@AutoRegisterActionEditor
public class AutoTakeActionEditor extends ActionEditorComponent<AutoTakeActionData> {

    public AutoTakeActionEditor(AutoTakeActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("AUTOT Action");

        Span description = new Span("The player picks up the item they named. The item is found from the noun of "
                                    + "the player's input, so it can serve every item at once.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Use it in a response for the wildcard noun (~).");

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
