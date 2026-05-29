package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

public class ConditionListEditor extends VerticalLayout {
    private final AdventureData adventureData;
    private final VerticalLayout rowsLayout;

    public ConditionListEditor(AdventureData adventureData) {
        this.adventureData = adventureData;

        rowsLayout = new VerticalLayout();
        rowsLayout.setPadding(false);
        rowsLayout.setSpacing(true);

        ConditionSelector selector = new ConditionSelector();
        selector.setConditionSelectedListener(data -> addRow(data, false));

        setPadding(false);
        add(rowsLayout, selector);
    }

    public void setConditions(List<PreConditionData> conditions) {
        rowsLayout.removeAll();
        if (conditions == null) return;
        for (PreConditionData data : conditions) {
            boolean negate = data instanceof NotConditionData;
            PreConditionData leaf = negate ? ((NotConditionData) data).getPreCondition() : data;
            addRow(leaf, negate);
        }
    }

    public List<PreConditionData> getConditions() {
        return rowsLayout.getChildren()
                .filter(c -> c instanceof ConditionRow)
                .map(c -> (ConditionRow) c)
                .map(ConditionRow::toConditionData)
                .collect(Collectors.toList());
    }

    private void addRow(PreConditionData data, boolean negate) {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(data, adventureData);
        ConditionRow row = new ConditionRow(editor, negate);
        row.setOnRemove(() -> rowsLayout.remove(row));
        rowsLayout.add(row);
    }
}
