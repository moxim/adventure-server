package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.action.NewLineActionData;

/**
 * Editor component for NewLineActionData.
 * The action has no configurable parameters: it ends the line, so the next text starts on a new line.
 */
@AutoRegisterActionEditor
public class NewLineActionEditor extends ActionEditorComponent<NewLineActionData> {

    public NewLineActionEditor(NewLineActionData actionData) {
        super(actionData);
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("NewLine Action");

        Span description = new Span("End the line, so the next text starts on a new line. This action takes no "
                                    + "parameters.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span info = new Span("ℹ Use it after Msg, SystemMsg or Print, which do not end the line. After a Message, "
                             + "which ends its line itself, it leaves an empty line.");

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
