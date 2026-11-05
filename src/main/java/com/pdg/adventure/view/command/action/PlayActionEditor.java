package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.model.action.PlayActionData;

/** Picks one of the adventure's sounds and how many times it plays in a row; 0 stops every copy of that sound. */
@AutoRegisterActionEditor
public class PlayActionEditor extends ActionEditorComponent<PlayActionData> {
    static final int MIN_TIMES = 0;
    static final int MAX_TIMES = 99;

    private final PlayActionData playActionData;
    private final AdventureData adventureData;
    private ComboBox<SoundData> soundComboBox;
    private IntegerField timesField;

    public PlayActionEditor(PlayActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.playActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Play Action");
        Span description = new Span("Play a sound this many times in a row, starting at once. "
                                    + "Sounds keep playing when the player moves on; 0 stops every copy of the sound");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<SoundData> sounds = new ArrayList<>(adventureData.getSoundData().values());
        sounds.sort(Comparator.comparing(SoundData::getName, String.CASE_INSENSITIVE_ORDER));

        soundComboBox = new ComboBox<>("Sound");
        soundComboBox.setItems(sounds);
        soundComboBox.setItemLabelGenerator(SoundData::getName);
        soundComboBox.setPlaceholder("Select a sound");
        soundComboBox.setWidthFull();
        soundComboBox.setRequired(true);
        if (playActionData.getSoundId() != null) {
            sounds.stream()
                  .filter(sound -> sound.getId().equals(playActionData.getSoundId()))
                  .findFirst()
                  .ifPresent(soundComboBox::setValue);
        }
        soundComboBox.addValueChangeListener(e -> playActionData.setSoundId(
                e.getValue() == null ? null : e.getValue().getId()));

        timesField = new IntegerField("Times");
        timesField.setMin(MIN_TIMES);
        timesField.setMax(MAX_TIMES);
        timesField.setStep(1);
        timesField.setStepButtonsVisible(true);
        timesField.setRequired(true);
        timesField.setHelperText("1 to 99 plays the sound that many times; 0 stops it");
        timesField.setValue(playActionData.getTimes());
        timesField.addValueChangeListener(e -> playActionData.setTimes(e.getValue()));

        add(title, description, soundComboBox, timesField);
    }

    @Override
    public boolean validate() {
        boolean soundValid = soundComboBox.getValue() != null;
        soundComboBox.setInvalid(!soundValid);
        if (!soundValid) {
            soundComboBox.setErrorMessage("Please select a sound");
        }
        Integer times = timesField.getValue();
        boolean timesValid = times != null && times >= MIN_TIMES && times <= MAX_TIMES;
        timesField.setInvalid(!timesValid);
        if (!timesValid) {
            timesField.setErrorMessage("Please enter a whole number from " + MIN_TIMES + " to " + MAX_TIMES);
        }
        return soundValid && timesValid;
    }

    @Override
    public String getActionSummary() {
        SoundData selected = soundComboBox == null ? null : soundComboBox.getValue();
        if (selected == null) {
            return "(none)";
        }
        Integer times = timesField.getValue();
        return selected.getName() + " " + (times == null ? "?" : times);
    }
}
