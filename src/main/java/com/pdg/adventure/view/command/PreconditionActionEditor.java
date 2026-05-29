package com.pdg.adventure.view.command;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.command.condition.ConditionListEditor;
import com.pdg.adventure.view.component.GridFactory;

public class PreconditionActionEditor extends VerticalLayout {
    private final ConditionListEditor conditionListEditor;
    private final Grid<ActionData> actionGrid;

    public PreconditionActionEditor(AdventureData adventureData) {
        conditionListEditor = new ConditionListEditor(adventureData);

        actionGrid = GridFactory.createGrid(ActionData.class, List.of(
                new GridFactory.ColumnConfig<>(a -> a.getId(), "ID", false)
        ));
        Button addAction = new Button("Add Action", _ -> addAction());

        Details preconditionsSection = new Details("Preconditions", conditionListEditor);
        Details actionsSection = new Details("Actions", new VerticalLayout(addAction, actionGrid));

        add(preconditionsSection, actionsSection);
    }

    public void setCommand(CommandData commandData) {
        conditionListEditor.setConditions(commandData.getPreConditions());
    }

    public void saveToCommand(CommandData commandData) {
        commandData.setPreConditions(conditionListEditor.getConditions());
    }

    private void addAction() {
        // Implement based on ActionData structure
    }
}
