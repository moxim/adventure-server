package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

public class ConditionRow extends Details {
    private final ConditionEditorComponent editor;
    private final Checkbox negateCheckbox;
    private final String virginTypeName;
    @Setter
    private Runnable onRemove;
    @Setter
    private Runnable onChange;
    @Setter
    private Runnable onMoveUp;
    @Setter
    private Runnable onMoveDown;

    public ConditionRow(ConditionEditorComponent editor, boolean negate) {
        this.editor = editor;
        this.virginTypeName = editor.getConditionData().getPreconditionName()
                                     .replace("ConditionData", "");

        negateCheckbox = new Checkbox("Negate", negate);
        refreshSummary();
        negateCheckbox.addValueChangeListener(e -> {
            refreshSummary();
            if (onChange != null) onChange.run();
        });

        Button upButton = new Button("Up");
        upButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        upButton.addClickListener(_ -> { if (onMoveUp != null) onMoveUp.run(); });

        Button downButton = new Button("Down");
        downButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        downButton.addClickListener(_ -> { if (onMoveDown != null) onMoveDown.run(); });

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(negateCheckbox, upButton, downButton, removeButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        add(controls, editor);
        setWidthFull();
    }

    /** Recompute the header from the editor's current target value and the negate state. */
    public void refreshSummary() {
        String typeName = Boolean.TRUE.equals(negateCheckbox.getValue()) ? "Not" + virginTypeName : virginTypeName;
        String summary = editor.getConditionSummary();
        String headerText = summary.isBlank() ? typeName : typeName + ": " + summary;
        setSummaryText(headerText);
    }

    public PreConditionData toConditionData() {
        if (Boolean.TRUE.equals(negateCheckbox.getValue())) {
            NotConditionData not = new NotConditionData();
            not.setPreCondition(editor.getConditionData());
            return not;
        }
        return editor.getConditionData();
    }

    public boolean validate() {
        return editor.validate();
    }
}
