package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.PictureGrid;

class PlaceLocationDialogTest extends BrowserlessTest {

    private final Word cellar = new Word("cellar", Word.Type.NOUN);
    private final Word attic = new Word("attic", Word.Type.NOUN);
    private final Word dark = new Word("dark", Word.Type.ADJECTIVE);

    private AdventureService service;
    private AdventureData adventure;
    private AtomicBoolean done;
    private PlaceLocationDialog dialog;

    @BeforeEach
    void setUp() {
        service = mock(AdventureService.class);
        adventure = new AdventureData();
        adventure.getVocabularyData().setWords(java.util.List.of(cellar, attic, dark));
        done = new AtomicBoolean();
    }

    private LocationData unplacedLocation(String anId, Word aNoun) {
        LocationData location = new LocationData();
        location.setId(anId);
        location.getDescriptionData().setNoun(aNoun);
        adventure.getLocationData().put(anId, location);
        return location;
    }

    private void open() {
        dialog = new PlaceLocationDialog(adventure, service, new PictureGrid.Cell(7, 3), () -> done.set(true));
        dialog.open();
    }

    private Button place() {
        return find(Button.class, dialog).withText("Place").single();
    }

    private VocabularyPickerField picker(String aLabel) {
        return find(VocabularyPickerField.class, dialog).withCaption(aLabel).single();
    }

    private void showNewTab() {
        find(Tabs.class, dialog).single().setSelectedTab(find(Tab.class, dialog).all().get(1));
    }

    @Test
    void placingAnUnplacedLocationStoresTheCellAndSavesOnlyThatLocation() {
        LocationData unplaced = unplacedLocation("u1", cellar);
        open();
        test(find(Grid.class, dialog).single()).select(0);

        test(place()).click();

        assertThat(unplaced.getMapColumn()).isEqualTo(7);
        assertThat(unplaced.getMapRow()).isEqualTo(3);
        verify(service).saveLocationData(unplaced);
        verify(service, never()).saveAdventureData(any());
        assertThat(done).isTrue();
    }

    @Test
    void withUnplacedLocationsPlaceStaysDisabledUntilOneIsChosen() {
        unplacedLocation("u1", cellar);
        open();

        assertThat(place().isEnabled()).isFalse();
        test(find(Grid.class, dialog).single()).select(0);
        assertThat(place().isEnabled()).isTrue();
    }

    @Test
    void thereAreTwoTabsWhenAnUnplacedLocationExists_andTheExistingOneIsShownFirst() {
        unplacedLocation("u1", cellar);
        open();

        assertThat(find(Tab.class, dialog).all()).extracting(Tab::getLabel)
                .containsExactly("Unplaced locations", "New location");
        assertThat(find(Grid.class, dialog).all()).hasSize(1);
        assertThat(find(VocabularyPickerField.class, dialog).all()).isEmpty();
    }

    @Test
    void creatingANewLocationPlacesItAndSavesTheAdventure() {
        unplacedLocation("u1", attic);
        open();
        showNewTab();
        picker("Noun").setValue(cellar);
        picker("Adjective").setValue(dark);
        test(find(TextField.class, dialog).withCaption("Short description").single()).setValue("a damp cellar");

        test(place()).click();

        LocationData created = adventure.getLocationData().values().stream()
                .filter(location -> cellar.equals(location.getDescriptionData().getNoun())).findFirst().orElseThrow();
        assertThat(created.getMapColumn()).isEqualTo(7);
        assertThat(created.getMapRow()).isEqualTo(3);
        assertThat(created.getDescriptionData().getAdjective()).isEqualTo(dark);
        assertThat(created.getDescriptionData().getShortDescription()).isEqualTo("a damp cellar");
        verify(service).saveAdventureData(adventure);
        verify(service, never()).saveLocationData(any());
        assertThat(done).isTrue();
    }

    @Test
    void theNewTabNeedsANounBeforePlaceIsEnabled() {
        unplacedLocation("u1", attic);
        open();
        showNewTab();

        assertThat(place().isEnabled()).isFalse();
        picker("Noun").setValue(cellar);
        assertThat(place().isEnabled()).isTrue();
    }

    @Test
    void whenNoLocationIsUnplaced_onlyTheNewLocationFormIsShown() {
        open();

        assertThat(find(Tabs.class, dialog).all()).isEmpty();
        assertThat(find(Grid.class, dialog).all()).isEmpty();
        picker("Noun");
        assertThat(place().isEnabled()).isFalse();
    }

    @Test
    void theNounsOfferedAreTheVocabularysNouns() {
        open();

        assertThat(picker("Noun").getListDataView().getItems()).containsExactlyInAnyOrder(cellar, attic);
        assertThat(picker("Adjective").getListDataView().getItems()).containsExactly(dark);
    }

    @Test
    void withoutAnyNounInTheVocabularyTheDialogSaysSo() {
        adventure.getVocabularyData().setWords(java.util.List.of(dark));
        open();

        assertThat(find(Paragraph.class, dialog).all()).anyMatch(
                paragraph -> paragraph.getText().contains("no noun yet"));
        assertThat(place().isEnabled()).isFalse();
    }

    @Test
    void cancelChangesAndSavesNothing() {
        open();

        test(find(Button.class, dialog).withText("Cancel").single()).click();

        verify(service, never()).saveAdventureData(any());
        verify(service, never()).saveLocationData(any());
        assertThat(adventure.getLocationData()).isEmpty();
        assertThat(done).isFalse();
    }
}
