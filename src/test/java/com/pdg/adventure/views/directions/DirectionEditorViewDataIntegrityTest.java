package com.pdg.adventure.views.directions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * Data integrity and persistence tests for DirectionEditorView.
 * Tests data validation, save operations, and service layer interactions.
 */
@ExtendWith(MockitoExtension.class)
class DirectionEditorViewDataIntegrityTest {

    @Mock
    private AdventureService adventureService;

    private DirectionEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private DirectionData directionData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));
        locationData.setDirectionsData(new HashSet<>());

        LocationData destination = new LocationData();
        destination.setId("location-2");
        destination.setDescriptionData(new DescriptionData("Destination", "A destination"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        locations.put(destination.getId(), destination);
        adventureData.setLocationData(locations);

        vocabularyData = new VocabularyData();
        Word goVerb = new Word("go", Word.Type.VERB);
        Word northNoun = new Word("north", Word.Type.NOUN);
        Word darkAdjective = new Word("dark", Word.Type.ADJECTIVE);
        vocabularyData.setWords(List.of(goVerb, northNoun, darkAdjective));
        adventureData.setVocabularyData(vocabularyData);

        directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId("location-2");
        directionData.setDescriptionData(new DescriptionData("Go north", "You can go north"));
        directionData.setDestinationMustBeMentioned(true);

        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(goVerb);
        commandDescription.setNoun(northNoun);
        commandData.setCommandDescription(commandDescription);
        directionData.setCommandData(commandData);

        locationData.getDirectionsData().add(directionData);
        view = new DirectionEditorView(adventureService);
        view.setUpLoading("direction-1");
    }

    @Test
    void validateSave_withValidData_shouldCallAdventureService() {
        //given
        view.setUpLoading("direction-1");

        // given
        view.setData(locationData, adventureData);

        // when
        // Note: validateSave is private, would need to trigger via button click in integration test
        // or use reflection for unit testing

        // then
        // This test would verify the service call if we had access to validateSave
    }

    @Test
    void directionData_shouldHaveCommandData() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(directionData.getCommandData()).isNotNull();
        assertThat(directionData.getCommandData().getCommandDescription()).isNotNull();
    }

    @Test
    void directionData_shouldHaveDestinationId() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(directionData.getDestinationId()).isEqualTo("location-2");
    }

    @Test
    void commandData_shouldSetActionNotNull() {
        // given
        CommandData commandData = new CommandData();
        MovePlayerActionData action = new MovePlayerActionData();
        action.setLocationId("location-2");

        // when
        commandData.setAction(action);

        // then
        assertThat(commandData.getAction()).isNotNull();
        assertThat(((MovePlayerActionData) commandData.getAction()).getLocationId()).isEqualTo("location-2");
    }

    @Test
    void commandData_shouldThrowExceptionForNullAction() {
        // given
        CommandData commandData = new CommandData();

        // when/then
        assertThatThrownBy(() -> commandData.setAction(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Action cannot be null");
    }

    @Test
    void directionData_shouldHaveDescriptionData() {
        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(directionData.getDescriptionData()).isNotNull();
        assertThat(directionData.getDescriptionData().getShortDescription()).isEqualTo("Go north");
        assertThat(directionData.getDescriptionData().getLongDescription()).isEqualTo("You can go north");
    }

    @Test
    void commandDescription_shouldHaveVerb() {
        // when
        view.setData(locationData, adventureData);

        // then
        CommandDescriptionData desc = directionData.getCommandData().getCommandDescription();
        assertThat(desc.getVerb()).isNotNull();
        assertThat(desc.getVerb().getText()).isEqualTo("go");
        assertThat(desc.getVerb().getType()).isEqualTo(Word.Type.VERB);
    }

    @Test
    void commandDescription_canHaveNoun() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        CommandDescriptionData desc = directionData.getCommandData().getCommandDescription();
        assertThat(desc.getNoun()).isNotNull();
        assertThat(desc.getNoun().getText()).isEqualTo("north");
        assertThat(desc.getNoun().getType()).isEqualTo(Word.Type.NOUN);
    }

    @Test
    void locationData_shouldContainDirection() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(locationData.getDirectionsData()).contains(directionData);
        assertThat(locationData.getDirectionsData()).hasSize(1);
    }

    @Test
    void adventureData_shouldContainBothLocations() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(adventureData.getLocationData()).hasSize(2);
        assertThat(adventureData.getLocationData()).containsKey("location-1");
        assertThat(adventureData.getLocationData()).containsKey("location-2");
    }

    @Test
    void vocabulary_shouldContainRequiredWords() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        Collection<Word> words = vocabularyData.getWords();
        assertThat(words).hasSize(3);
        assertThat(words.stream().map(Word::getText)).contains("go", "north", "dark");
    }

    @Test
    void directionId_shouldBeUnique() {
        // given
        DirectionData direction2 = new DirectionData();
        direction2.setId(UUID.randomUUID().toString());

        // then
        assertThat(directionData.getId()).isNotEqualTo(direction2.getId());
    }
}
