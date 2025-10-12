package com.pdg.adventure.views.commands;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.views.components.GridFactory;

public class PreconditionActionEditor extends VerticalLayout {
    private final Grid<PreConditionData> preconditionGrid;
    private final Grid<ActionData> actionGrid;

    public PreconditionActionEditor() {
        preconditionGrid = GridFactory.createGrid(PreConditionData.class, List.of(
            new GridFactory.ColumnConfig<>(p -> p.getId(), "ID", false)
        ));
        actionGrid = GridFactory.createGrid(ActionData.class, List.of(
            new GridFactory.ColumnConfig<>(a -> a.getId(), "ID", false)
        ));
        Button addPrecondition = new Button("Add Precondition", e -> addPrecondition());
        Button addAction = new Button("Add Action", e -> addAction());
        Details details = new Details("Preconditions & Actions", new VerticalLayout(
            addPrecondition, preconditionGrid, addAction, actionGrid));
        add(details);
    }

    private void addPrecondition() {
        // Implement based on PreConditionData structure
    }

    private void addAction() {
        // Implement based on ActionData structure
    }
}
