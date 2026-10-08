package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.LoadGameActionData;

/**
 * Editor component for LoadGameActionData.
 * The action has no configurable parameters: without a slot number (LOAD) it lists the player's saved games, with
 * one (LOAD 3) it restores that game, so it is meant for a response for the verb LOAD with the wildcard noun (~).
 */
@AutoRegisterActionEditor
public class LoadGameActionEditor extends ActionEditorComponent<LoadGameActionData> {

    public LoadGameActionEditor(LoadGameActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Load Game Action");

        Span description = new Span("Without a slot number (LOAD) lists the player's saved games for this "
                                    + "adventure; with one (LOAD 3) restores that game and describes the place the "
                                    + "player is in again.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Use it in a response for the verb LOAD with the "
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
