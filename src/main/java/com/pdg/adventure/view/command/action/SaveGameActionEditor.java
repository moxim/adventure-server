package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.SaveGameActionData;

/**
 * Editor component for SaveGameActionData.
 * The action has no configurable parameters: it saves into the slot the player typed (SAVE 3) or the first free one
 * (SAVE), so it is meant for a response for the verb SAVE with the wildcard noun (~).
 */
@AutoRegisterActionEditor
public class SaveGameActionEditor extends ActionEditorComponent<SaveGameActionData> {

    public SaveGameActionEditor(SaveGameActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Save Game Action");

        Span description = new Span("Saves the running game into a slot of the current player: the slot number "
                                    + "the player typed (SAVE 3), or the first free one (SAVE). Each player has "
                                    + "10 slots per adventure.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Use it in a response for the verb SAVE with the "
                             + "wildcard noun (~).");

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
