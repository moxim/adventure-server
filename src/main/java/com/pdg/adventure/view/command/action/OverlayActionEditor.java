package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.action.OverlayActionData;
import com.pdg.adventure.view.support.PictureGrid;

@AutoRegisterActionEditor
public class OverlayActionEditor extends ActionEditorComponent<OverlayActionData> {

    private static final int MIN_CELL = 0;
    private static final int MAX_CELL = PictureGrid.SIZE - 1;

    private final OverlayActionData overlayActionData;
    private final AdventureData adventureData;
    private IntegerField xField;
    private IntegerField yField;
    private ComboBox<PictureData> pictureComboBox;

    public OverlayActionEditor(OverlayActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.overlayActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Overlay Action");
        // TODO: Review needed — the overlay fills exactly one cell (stretched) and goes when the picture on show changes.
        Span description = new Span("Lay a picture over one cell of the picture on show, e.g. an open door over the "
                                    + "closed one. Does nothing if no picture is showing. The overlay is stretched to "
                                    + "the cell and stays until the picture changes.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        xField = cellField("X (column, 0–" + MAX_CELL + ")", "Counted from the left, as on the world map.");
        yField = cellField("Y (row, 0–" + MAX_CELL + ")", "Counted from the top, as on the world map.");
        if (overlayActionData.getX() != null) {
            xField.setValue(overlayActionData.getX());
        }
        if (overlayActionData.getY() != null) {
            yField.setValue(overlayActionData.getY());
        }
        xField.addValueChangeListener(e -> overlayActionData.setX(e.getValue()));
        yField.addValueChangeListener(e -> overlayActionData.setY(e.getValue()));

        List<PictureData> pictures = new ArrayList<>(adventureData.getPictureData().values());
        pictures.sort(Comparator.comparing(PictureData::getName, String.CASE_INSENSITIVE_ORDER));

        pictureComboBox = new ComboBox<>("Overlay picture");
        pictureComboBox.setItems(pictures);
        pictureComboBox.setItemLabelGenerator(PictureData::getName);
        pictureComboBox.setPlaceholder("Select a picture");
        pictureComboBox.setWidthFull();
        pictureComboBox.setRequired(true);
        if (overlayActionData.getPictureId() != null) {
            pictures.stream()
                    .filter(picture -> picture.getId().equals(overlayActionData.getPictureId()))
                    .findFirst()
                    .ifPresent(pictureComboBox::setValue);
        }
        pictureComboBox.addValueChangeListener(e -> overlayActionData.setPictureId(
                e.getValue() == null ? null : e.getValue().getId()));

        add(title, description, xField, yField, pictureComboBox);
    }

    private static IntegerField cellField(String aLabel, String aTooltip) {
        IntegerField field = new IntegerField(aLabel);
        field.setMin(MIN_CELL);
        field.setMax(MAX_CELL);
        field.setWidthFull();
        field.setRequired(true);
        field.setTooltipText("The cell of the " + PictureGrid.SIZE + " x " + PictureGrid.SIZE
                             + " grid over the picture that gets the overlay. " + aTooltip);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        return field;
    }

    @Override
    public boolean validate() {
        boolean xValid = validateCell(xField, "column");
        boolean yValid = validateCell(yField, "row");
        boolean pictureValid = pictureComboBox.getValue() != null;
        pictureComboBox.setInvalid(!pictureValid);
        if (!pictureValid) {
            pictureComboBox.setErrorMessage("Please select a picture");
        }
        return xValid && yValid && pictureValid;
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
    public String getActionSummary() {
        PictureData selected = pictureComboBox == null ? null : pictureComboBox.getValue();
        if (xField == null || yField == null || xField.getValue() == null || yField.getValue() == null
            || selected == null) {
            return "(none)";
        }
        return xField.getValue() + ", " + yField.getValue() + " " + selected.getName();
    }
}
