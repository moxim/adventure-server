package com.pdg.adventure.view.command;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.view.command.action.ActionListEditor;
import com.pdg.adventure.view.command.condition.ConditionListEditor;

public class PreconditionActionEditor extends VerticalLayout {
    private final ConditionListEditor conditionListEditor;
    private final ActionListEditor actionListEditor;

    public PreconditionActionEditor(AdventureData adventureData) {
        conditionListEditor = new ConditionListEditor(adventureData);
        actionListEditor = new ActionListEditor(adventureData);

        Details preconditionsSection = new Details("Preconditions", conditionListEditor);
        Details actionsSection = new Details("Actions", actionListEditor);

        setPadding(false);
        add(preconditionsSection, actionsSection);
    }

    public void setCommand(CommandData commandData) {
        conditionListEditor.setConditions(commandData.getPreConditions());
        actionListEditor.setActions(commandData.getActions());
    }

    public void saveToCommand(CommandData commandData) {
        commandData.setPreConditions(conditionListEditor.getConditions());
        commandData.setActions(actionListEditor.getActions());
    }

    public boolean validate() {
        return actionListEditor.validate();
    }

    /** Wire a single change callback to both editors so the host view can track dirtiness. */
    public void setOnChange(Runnable onChange) {
        conditionListEditor.setOnChange(onChange);
        actionListEditor.setOnChange(onChange);
    }
}
