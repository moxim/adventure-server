package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.LookActionData;

/**
 * Editor component for LookActionData. The action has no configurable parameters, so this editor is a pure
 * informational panel.
 */
@AutoRegisterActionEditor
public class LookActionEditor extends ActionEditorComponent<LookActionData> {

    public LookActionEditor(LookActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Look Action");

        Span description = new Span("Describe the location the player is in, with its picture, then fire the Arrival Processes.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Use it in a response for your look verb, e.g. describe (and describe here). Not in an Arrival Process: Look already fires those.");

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
