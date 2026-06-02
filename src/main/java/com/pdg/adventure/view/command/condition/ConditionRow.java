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
    @Setter
    private Runnable onRemove;

    public ConditionRow(ConditionEditorComponent editor, boolean negate) {
        this.editor = editor;

        String virginTypeName = editor.getConditionData().getPreconditionName()
                                       .replace("ConditionData", "");

        negateCheckbox = new Checkbox("Negate", negate);
        determineSummaryText(editor, virginTypeName);
        negateCheckbox.addValueChangeListener(e -> {
            determineSummaryText(editor, virginTypeName);
        });

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(negateCheckbox, removeButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        add(controls, editor);
        setWidthFull();
    }

    private void determineSummaryText(final ConditionEditorComponent editor, final String virginTypeName) {
        String typeName = virginTypeName;
        if (Boolean.TRUE.equals(negateCheckbox.getValue())) {
            typeName = "Not" + typeName;
        } else {
            typeName = virginTypeName;
        }
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
