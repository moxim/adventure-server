package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.value.ValueChangeMode;

import com.pdg.adventure.model.condition.ClickedConditionData;
import com.pdg.adventure.view.support.PictureGrid;

@AutoRegisterConditionEditor
public class ClickedConditionEditor extends ConditionEditorComponent<ClickedConditionData> {

    private static final int MIN_CELL = 0;
    private static final int MAX_CELL = PictureGrid.SIZE - 1;

    private final ClickedConditionData typedCondition;
    private IntegerField xField;
    private IntegerField yField;

    public ClickedConditionEditor(ClickedConditionData conditionData) {
        super(conditionData);
        this.typedCondition = conditionData;
    }

    @Override
    protected void buildUI() {
        xField = cellField("X (column, 0–" + MAX_CELL + ")", "Counted from the left, as on the world map.");
        yField = cellField("Y (row, 0–" + MAX_CELL + ")", "Counted from the top, as on the world map.");

        if (typedCondition.getX() != null) {
            xField.setValue(typedCondition.getX());
        }
        if (typedCondition.getY() != null) {
            yField.setValue(typedCondition.getY());
        }
        xField.addValueChangeListener(e -> typedCondition.setX(e.getValue()));
        yField.addValueChangeListener(e -> typedCondition.setY(e.getValue()));

        add(xField, yField);
    }

    private static IntegerField cellField(String aLabel, String aTooltip) {
        IntegerField field = new IntegerField(aLabel);
        field.setMin(MIN_CELL);
        field.setMax(MAX_CELL);
        field.setWidthFull();
        field.setRequired(true);
        field.setTooltipText("Succeeds when the player clicked this cell of the " + PictureGrid.SIZE + " x "
                             + PictureGrid.SIZE + " grid over the picture. " + aTooltip);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    @Override
    public boolean validate() {
        boolean xValid = validateCell(xField, "column");
        boolean yValid = validateCell(yField, "row");
        return xValid && yValid;
    }

    private static boolean validateCell(IntegerField aField, String aName) {
        Integer value = aField.getValue();
        boolean valid = value != null && value >= MIN_CELL && value <= MAX_CELL;
        aField.setInvalid(!valid);
        if (!valid) {
            aField.setErrorMessage("Please enter a " + aName + " between " + MIN_CELL + " and " + MAX_CELL);
        }
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (xField == null || yField == null || xField.getValue() == null || yField.getValue() == null) {
            return "(none)";
        }
        return "clicked " + xField.getValue() + ", " + yField.getValue();
    }
}
