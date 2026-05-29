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

        String typeName = editor.getConditionData().getPreconditionName()
                               .replace("ConditionData", "");
        setSummaryText(typeName);

        negateCheckbox = new Checkbox("Negate", negate);

        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(_ -> { if (onRemove != null) onRemove.run(); });

        HorizontalLayout controls = new HorizontalLayout(negateCheckbox, removeButton);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        add(controls, editor);
        setWidthFull();
    }

    public PreConditionData toConditionData() {
        if (negateCheckbox.getValue()) {
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
