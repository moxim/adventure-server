package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.ActionData;

public class ActionListEditor extends VerticalLayout {
    private final AdventureData adventureData;
    private final VerticalLayout rowsLayout;
    @Setter
    private Runnable onChange;   // fired on any user-driven edit (add/remove/reorder/leaf-field)

    public ActionListEditor(AdventureData anAdventureData) {
        adventureData = anAdventureData;

        rowsLayout = new VerticalLayout();
        rowsLayout.setPadding(false);
        rowsLayout.setSpacing(true);

        ActionSelector selector = new ActionSelector(adventureData);
        selector.setEditorSelectedListener(editor -> { addRow(editor); notifyChange(); });

        setPadding(false);
        add(rowsLayout, selector);
    }

    public void setActions(List<ActionData> actions) {
        rowsLayout.removeAll();
        if (actions == null) return;
        for (ActionData data : actions) {
            addRow(ActionEditorFactory.createEditor(data, adventureData));  // programmatic load: no notifyChange
        }
    }

    public List<ActionData> getActions() {
        return rowsLayout.getChildren()
                .filter(ActionRow.class::isInstance)
                .map(ActionRow.class::cast)
                .map(ActionRow::toActionData)
                .collect(Collectors.toList());
    }

    public boolean validate() {
        return rowsLayout.getChildren()
                .filter(ActionRow.class::isInstance)
                .map(ActionRow.class::cast)
                .allMatch(ActionRow::validate);
    }

    private void addRow(ActionEditorComponent editor) {
        ActionRow row = new ActionRow(editor);
        row.setOnRemove(() -> { rowsLayout.remove(row); notifyChange(); });
        row.setOnMoveUp(() -> moveRow(row, -1));
        row.setOnMoveDown(() -> moveRow(row, 1));
        // On leaf-field edits: refresh the row header live, and mark dirty for client edits
        // (ports the old CommandEditorView.attachActionEditorListeners).
        editor.getChildren().forEach(child -> {
            if (child instanceof HasValue<?, ?> hasValue) {
                hasValue.addValueChangeListener(e -> {
                    row.refreshSummary();
                    if (e.isFromClient()) notifyChange();
                });
            }
        });
        rowsLayout.add(row);
    }

    private void moveRow(ActionRow row, int delta) {
        List<Component> children = rowsLayout.getChildren().collect(Collectors.toList());
        int newIndex = children.indexOf(row) + delta;
        if (newIndex < 0 || newIndex >= children.size()) return;
        rowsLayout.remove(row);
        rowsLayout.addComponentAtIndex(newIndex, row);
        notifyChange();
    }

    private void notifyChange() {
        if (onChange != null) onChange.run();
    }
}
