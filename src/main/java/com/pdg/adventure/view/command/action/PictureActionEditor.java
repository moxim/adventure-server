package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.action.PictureActionData;

@AutoRegisterActionEditor
public class PictureActionEditor extends ActionEditorComponent<PictureActionData> {
    private final PictureActionData pictureActionData;
    private final AdventureData adventureData;
    private ComboBox<PictureData> pictureComboBox;

    public PictureActionEditor(PictureActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.pictureActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Picture Action");
        Span description = new Span("Show a picture to the player until the next move, look, or picture action");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<PictureData> pictures = new ArrayList<>(adventureData.getPictureData().values());
        pictures.sort(Comparator.comparing(PictureData::getName, String.CASE_INSENSITIVE_ORDER));

        pictureComboBox = new ComboBox<>("Picture");
        pictureComboBox.setItems(pictures);
        pictureComboBox.setItemLabelGenerator(PictureData::getName);
        pictureComboBox.setPlaceholder("Select a picture");
        pictureComboBox.setWidthFull();
        pictureComboBox.setRequired(true);

        if (pictureActionData.getPictureId() != null) {
            pictures.stream()
                    .filter(picture -> picture.getId().equals(pictureActionData.getPictureId()))
                    .findFirst()
                    .ifPresent(pictureComboBox::setValue);
        }

        pictureComboBox.addValueChangeListener(e -> pictureActionData.setPictureId(
                e.getValue() == null ? null : e.getValue().getId()));

        add(title, description, pictureComboBox);
    }

    @Override
    public boolean validate() {
        boolean isValid = pictureComboBox.getValue() != null;
        pictureComboBox.setInvalid(!isValid);
        if (!isValid) {
            pictureComboBox.setErrorMessage("Please select a picture");
        }
        return isValid;
    }

    @Override
    public String getActionSummary() {
        PictureData selected = pictureComboBox == null ? null : pictureComboBox.getValue();
        return selected == null ? "(none)" : selected.getName();
    }
}
