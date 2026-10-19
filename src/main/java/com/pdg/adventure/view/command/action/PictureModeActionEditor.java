package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.model.action.PictureModeActionData;

/**
 * Editor component for PictureModeActionData: the author picks ON, OFF or DEFAULT, which decides whether a
 * location's picture is shown when the player arrives there.
 */
@AutoRegisterActionEditor
public class PictureModeActionEditor extends ActionEditorComponent<PictureModeActionData> {
    private final PictureModeActionData pictureModeActionData;
    private ComboBox<PictureMode> modeSelector;

    public PictureModeActionEditor(PictureModeActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.pictureModeActionData = actionData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("PictureMode Action");
        Span description = new Span("Decide when the picture of a location is shown on arrival. A Look or a Picture "
                                    + "action shows a picture in every mode.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        modeSelector = new ComboBox<>("Mode");
        modeSelector.setItems(PictureMode.values());
        modeSelector.setItemLabelGenerator(PictureModeActionEditor::label);
        modeSelector.setPlaceholder("Select a mode");
        modeSelector.setWidthFull();
        modeSelector.setRequired(true);
        modeSelector.setValue(pictureModeActionData.getMode());
        modeSelector.addValueChangeListener(e -> pictureModeActionData.setMode(e.getValue()));

        add(title, description, modeSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = modeSelector.getValue() != null;
        if (!valid) {
            modeSelector.setErrorMessage("Please select a mode");
        }
        modeSelector.setInvalid(!valid);
        return valid;
    }

    @Override
    public String getActionSummary() {
        PictureMode selected = modeSelector == null ? null : modeSelector.getValue();
        return selected == null ? "(none)" : selected.name();
    }

    private static String label(PictureMode aMode) {
        return switch (aMode) {
            case ON -> "ON: the picture of a location is shown on every arrival";
            case OFF -> "OFF: no picture is shown on arrival";
            case DEFAULT -> "DEFAULT: the picture is shown on the first arrival only";
        };
    }
}
