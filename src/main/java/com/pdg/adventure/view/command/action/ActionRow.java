package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import com.pdg.adventure.model.action.ActionData;

public class ActionRow extends Details {
    private final ActionEditorComponent editor;
    @Setter
    private Runnable onRemove;
    @Setter
    private Runnable onMoveUp;
    @Setter
    private Runnable onMoveDown;

    public ActionRow(ActionEditorComponent anEditor) {
        editor = anEditor;
        refreshSummary();

        Button upButton = new Button("Up");
        upButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        upButton.addClickListener(_ -> { if (onMoveUp != null) onMoveUp.run(); });

        Button downButton = new Button("Down");
        downButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        downButton.addClickListener(_ -> { if (onMoveDown != null) onMoveDown.run(); });

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(upButton, downButton, removeButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        add(controls, anEditor);
        setWidthFull();
    }

    /** Recompute the header from the editor's current target value. */
    public void refreshSummary() {
        String typeName = editor.getActionTypeName().replace("ActionData", "");
        String summary = editor.getActionSummary();
        setSummaryText(summary.isBlank() ? typeName : typeName + ": " + summary);
    }

    public ActionData toActionData() {
        return editor.getActionData();
    }

    public boolean validate() {
        return editor.validate();
    }
}
