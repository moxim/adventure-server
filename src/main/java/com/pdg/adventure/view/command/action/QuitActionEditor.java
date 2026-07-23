package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.QuitActionData;

/**
 * Editor component for QuitActionData.
 * The quit action has no configurable parameters — it terminates the game when executed.
 * This editor is a pure informational panel.
 */
@AutoRegisterActionEditor
public class QuitActionEditor extends ActionEditorComponent<QuitActionData> {

    public QuitActionEditor(QuitActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Quit Action");

        Span description = new Span("Terminate the game when this command is executed.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ This action takes no parameters. Precede it with a Message action to show a farewell message.");

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
