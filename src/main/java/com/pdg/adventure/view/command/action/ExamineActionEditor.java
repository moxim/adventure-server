package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.ExamineActionData;

/**
 * Editor component for ExamineActionData. The action has no configurable parameters, so this editor is a pure
 * informational panel.
 */
@AutoRegisterActionEditor
public class ExamineActionEditor extends ActionEditorComponent<ExamineActionData> {

    public ExamineActionEditor(ExamineActionData actionData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Examine Action");

        Span description = new Span("Show the long description of the item the player named, whether carried or here. The item is found from the noun of the player's input, so it can serve every item at once.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Use it in a response for your look verb with the wildcard noun (~).");

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
