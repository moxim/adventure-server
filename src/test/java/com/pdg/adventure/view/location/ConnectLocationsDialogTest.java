package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.direction.DirectionBuilder;

class ConnectLocationsDialogTest extends BrowserlessTest {

    private final Word go = new Word("go", Word.Type.VERB);
    private final Word enter = new Word("enter", Word.Type.VERB);
    private final Word north = new Word("north", Word.Type.NOUN);
    private final Word south = new Word("south", Word.Type.NOUN);
    private final Word door = new Word("door", Word.Type.NOUN);

    private AdventureService service;
    private AdventureData adventure;
    private LocationData source;
    private LocationData target;
    private AtomicBoolean done;
    private List<DirectionData> edited;
    private ConnectLocationsDialog dialog;

    @BeforeEach
    void setUp() {
        service = mock(AdventureService.class);
        adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.getVocabularyData().setWords(List.of(go, enter, north, south, door));
        source = location("source", "attic");
        target = location("target", "cellar");
        done = new AtomicBoolean();
        edited = new ArrayList<>();
    }

    private LocationData location(String anId, String aNoun) {
        LocationData location = new LocationData();
        location.setId(anId);
        location.getDescriptionData().setNoun(new Word(aNoun, Word.Type.NOUN));
        adventure.getLocationData().put(anId, location);
        return location;
    }

    private void open(LocationData aSource, LocationData aTarget) {
        dialog = new ConnectLocationsDialog(adventure, service, aSource, aTarget, () -> done.set(true)) {
            @Override
            void editDirection(DirectionData aDirection) {
                edited.add(aDirection);
            }
        };
        dialog.open();
    }

    private List<VocabularyPickerField> pickers() {
        return find(VocabularyPickerField.class, dialog).all();
    }

    private VocabularyPickerField picker(String aLabel) {
        return pickers().stream().filter(picker -> aLabel.equals(picker.getLabel())).findFirst().orElseThrow();
    }

    private Button save() {
        return find(Button.class, dialog).withText("Save").single();
    }

    private void tickOpposite() {
        test(find(Checkbox.class, dialog).single()).click();
    }

    private static String moveTarget(DirectionData aDirection) {
        return ((MovePlayerActionData) aDirection.getCommandData().getActions().getFirst()).getLocationId();
    }

    @Test
    void theSelectorsOfAllWordTypesOfferOnlyRootWords() {
        // the parser resolves a typed synonym to its root, so a direction holding a synonym is never followed
        Word climb = adventure.getVocabularyData().createSynonym("climb", enter);
        Word large = new Word("large", Word.Type.ADJECTIVE);
        adventure.getVocabularyData().addWord(large);
        Word big = adventure.getVocabularyData().createSynonym("big", large);
        Word gate = adventure.getVocabularyData().createSynonym("gate", door);
        open(source, target);
        tickOpposite();

        for (String label : List.of("Verb", "Opposite verb")) {
            assertThat(picker(label).getListDataView().getItems()).contains(enter).doesNotContain(climb);
        }
        for (String label : List.of("Adjective", "Opposite adjective")) {
            assertThat(picker(label).getListDataView().getItems()).contains(large).doesNotContain(big);
        }
        for (String label : List.of("Noun", "Opposite noun")) {
            assertThat(picker(label).getListDataView().getItems()).contains(door).doesNotContain(gate);
        }
    }

    @Test
    void savingANewDirectionCreatesItOnTheSourceWithTheChosenWords() {
        open(source, target);
        picker("Verb").setValue(go);
        picker("Noun").setValue(north);

        test(save()).click();

        assertThat(source.getDirectionsData()).hasSize(1);
        DirectionData created = source.getDirectionsData().iterator().next();
        assertThat(created.getDestinationId()).isEqualTo("target");
        assertThat(moveTarget(created)).isEqualTo("target");
        assertThat(created.getCommandData().getCommandDescription().getVerb()).isEqualTo(go);
        assertThat(created.getCommandData().getCommandDescription().getNoun()).isEqualTo(north);
        verify(service).saveLocationData(source);
        assertThat(done).isTrue();
    }

    @Test
    void theOppositeCheckboxRevealsSecondPickersAndCreatesBothDirections() {
        open(source, target);
        assertThat(pickers()).extracting(VocabularyPickerField::getLabel).doesNotContain("Opposite verb");

        tickOpposite();

        assertThat(pickers()).extracting(VocabularyPickerField::getLabel)
                             .contains("Opposite verb", "Opposite adjective", "Opposite noun");
        picker("Verb").setValue(go);
        picker("Noun").setValue(north);
        picker("Opposite verb").setValue(go);
        picker("Opposite noun").setValue(south);

        test(save()).click();

        assertThat(source.getDirectionsData()).hasSize(1);
        assertThat(target.getDirectionsData()).hasSize(1);
        DirectionData back = target.getDirectionsData().iterator().next();
        assertThat(back.getDestinationId()).isEqualTo("source");
        assertThat(back.getCommandData().getCommandDescription().getNoun()).isEqualTo(south);
        verify(service).saveLocationData(source);
        verify(service).saveLocationData(target);
    }

    @Test
    void saveStaysDisabledUntilTheVerbIsPickedAlsoForTheOppositeDirection() {
        open(source, target);
        assertThat(save().isEnabled()).isFalse();

        picker("Verb").setValue(go);
        assertThat(save().isEnabled()).isTrue();

        tickOpposite();
        assertThat(save().isEnabled()).isFalse();
        picker("Opposite verb").setValue(go);
        assertThat(save().isEnabled()).isTrue();
    }

    @Test
    void existingDirectionsAreListedWithEditAndAddAnother() {
        DirectionData existing = DirectionBuilder.create(source, "target", go, null, north);
        open(source, target);

        assertThat(pickers()).isEmpty();
        assertThat(find(Button.class, dialog).withText("Add another direction").all()).hasSize(1);

        test(find(Button.class, dialog).withText("Edit").single()).click();
        assertThat(edited).containsExactly(existing);

        test(find(Button.class, dialog).withText("Add another direction").single()).click();
        assertThat(pickers()).extracting(VocabularyPickerField::getLabel).contains("Verb");
    }

    @Test
    void sameWordsOnTheSourceAskForConfirmationAndSaveNothingUntilConfirmed() {
        DirectionBuilder.create(source, "elsewhere", go, null, north);
        open(source, target);
        picker("Verb").setValue(go);
        picker("Noun").setValue(north);

        test(save()).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getText()).contains("go");
        verify(service, never()).saveLocationData(any());
        assertThat(source.getDirectionsData()).hasSize(1);

        test(confirm).confirm();

        assertThat(source.getDirectionsData()).hasSize(2);
        verify(service).saveLocationData(source);
    }

    @Test
    void theHeaderNamesBothLocationsWithoutStrayBlanks() {
        open(source, target);

        assertThat(dialog.getHeaderTitle()).isEqualTo("Connect attic to cellar");
    }

    @Test
    void sameWordsOnTheTargetForTheOppositeDirectionAskForConfirmationToo() {
        DirectionBuilder.create(target, "elsewhere", go, null, south);
        open(source, target);
        tickOpposite();
        picker("Verb").setValue(go);
        picker("Noun").setValue(north);
        picker("Opposite verb").setValue(go);
        picker("Opposite noun").setValue(south);

        test(save()).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        verify(service, never()).saveLocationData(any());
        test(confirm).cancel();
        assertThat(source.getDirectionsData()).isEmpty();
        assertThat(target.getDirectionsData()).hasSize(1);
        assertThat(done).isFalse();
    }

    @Test
    void connectingALocationToItselfAsksTheDangerousDestinationQuestion() {
        open(source, source);
        picker("Verb").setValue(enter);
        picker("Noun").setValue(door);

        test(save()).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getHeader()).isEqualTo("Dangerous Destination");
        verify(service, never()).saveLocationData(any());
        test(confirm).confirm();
        verify(service).saveLocationData(source);
        assertThat(source.getDirectionsData()).hasSize(1);
    }

    @Test
    void withoutAnyVerbInTheVocabularyAHintIsShownAndSaveStaysDisabled() {
        adventure.getVocabularyData().setWords(List.of(north));
        open(source, target);

        assertThat(dialog.getElement().getTextRecursively()).containsIgnoringCase("verb");
        assertThat(save().isEnabled()).isFalse();
    }

    @Test
    void aFailedSaveIsReportedAndTheDialogClosedSoNothingCanBeReplayed() {
        doThrow(new IllegalStateException("db down")).when(service).saveLocationData(any());
        open(source, target);
        picker("Verb").setValue(go);

        test(save()).click();

        assertThat(find(Notification.class).all().stream().map(n -> test(n).getText()).toList())
                .anyMatch(text -> text.contains("Could not save"));
        assertThat(dialog.isOpened()).isFalse();
        assertThat(done).isTrue();
        assertThat(source.getDirectionsData()).hasSize(1);
    }
}
