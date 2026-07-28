package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.BreakActionData;

/**
 * Editor component for BreakActionData.
 * The break action has no configurable parameters - it stops the enclosing command chain from
 * running any further commands. This editor is a pure informational panel.
 */
@AutoRegisterActionEditor
public class BreakActionEditor extends ActionEditorComponent<BreakActionData> {

    public BreakActionEditor(BreakActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Break Action");

        Span description = new Span(
                "Stop processing this command chain here - later commands in the same chain "
                + "(with the same verb/adjective/noun) will not run. Other command chains are unaffected.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Place it after the actions that "
                              + "should run for this branch.");

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
