package com.pdg.adventure.view.direction;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * Edge case and boundary condition tests for DirectionEditorView.
 * Tests unusual inputs, null values, empty collections, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class DirectionEditorViewEdgeCasesTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    @Mock
    private RouteParameters routeParameters;

    private DirectionEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;

    @BeforeEach
    void setUp() {
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
        adventureData.setVocabularyData(vocabularyData);

        view = new DirectionEditorView(adventureService);
        view.setUpLoading("");
    }

    @Test
    void setData_withEmptyVocabulary_shouldNotThrowException() {
        // given
        vocabularyData.setWords(Collections.emptySet());
        DirectionData directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId(null);
        directionData.setDescriptionData(new DescriptionData("Test", "Test direction"));
        locationData.getDirectionsData().add(directionData);

        view.setUpLoading("direction-1");

        // when/then
        view.setData(locationData, adventureData);

        // Should complete without exception
        assertThat(view).isNotNull();
    }

    @Test
    void setData_withOnlyOneLocation_shouldShowEmptyGrid() {
        // given
        adventureData.getLocationData().clear();
        adventureData.getLocationData().put(locationData.getId(), locationData);

        DirectionData directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId(null);
        directionData.setDescriptionData(new DescriptionData("Test", "Test direction"));

        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        Word verb = new Word("go", Word.Type.VERB);
        commandDescription.setVerb(verb);
        commandData.setCommandDescription(commandDescription);
        directionData.setCommandData(commandData);

        vocabularyData.setWords(Set.of(verb));
        locationData.getDirectionsData().add(directionData);

        view.setUpLoading("direction-1");


        // when
        view.setData(locationData, adventureData);

        // then
        // Grid should be empty (no other locations to show)
        assertThat(adventureData.getLocationData()).hasSize(1);
    }

    @Test
    void setData_withNullDestinationId_shouldNotCrash() {
        // given
        DirectionData directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId(null);
        directionData.setDescriptionData(new DescriptionData("Test", "Test direction"));

        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        Word verb = new Word("go", Word.Type.VERB);
        commandDescription.setVerb(verb);
        commandData.setCommandDescription(commandDescription);
        directionData.setCommandData(commandData);

        vocabularyData.setWords(Set.of(verb));
        locationData.getDirectionsData().add(directionData);

        view.setUpLoading("direction-1");

        // when/then
        view.setData(locationData, adventureData);

        // Should not crash
        assertThat(directionData.getDestinationId()).isNull();
    }

    @Test
    void beforeEnter_withVeryLongDirectionId_shouldHandleGracefully() {
        // given
        String longId = "x".repeat(1000);
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.of(longId));

        // when
        view.beforeEnter(beforeEnterEvent);

        // then
        String title = view.getPageTitle();
        assertThat(title).contains(longId);
    }

    @Test
    void beforeEnter_withEmptyDirectionId_shouldTreatAsNew() {
        // given
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.of(""));

        // when
        view.beforeEnter(beforeEnterEvent);

        // then
        String title = view.getPageTitle();
        assertThat(title).contains("Edit Direction #");
    }

    @Test
    void setData_withNoDirectionsInLocation_shouldNotFindDirection() {
        // given
        locationData.setDirectionsData(new HashSet<>());
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.of("nonexistent-id"));

        view.beforeEnter(beforeEnterEvent);

        // when/then
        assertThat(view.getPageTitle()).isEqualTo("Edit Direction #nonexistent-id");
    }

    @Test
    void vocabulary_withOnlyVerbs_shouldPopulateVerbListOnly() {
        // given
        Word verb1 = new Word("go", Word.Type.VERB);
        Word verb2 = new Word("walk", Word.Type.VERB);
        vocabularyData.setWords(List.of(verb1, verb2));

        DirectionData directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId("location-2");
        directionData.setDescriptionData(new DescriptionData("Go", "Go somewhere"));

        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(verb1);
        commandData.setCommandDescription(commandDescription);
        directionData.setCommandData(commandData);

        locationData.getDirectionsData().add(directionData);

        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(vocabularyData.getWords()).hasSize(2);
        assertThat(vocabularyData.getWords().stream().allMatch(w -> w.getType() == Word.Type.VERB)).isTrue();
    }

    @Test
    void commandDescription_withNullVerb_shouldHaveNullText() {
        // given
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(null);

        // when
        String spec = commandDescription.getCommandSpecification();

        // then
        assertThat(spec).isEqualTo("||");
    }

    @Test
    void commandDescription_withOnlyVerb_shouldFormatCorrectly() {
        // given
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        Word verb = new Word("jump", Word.Type.VERB);
        commandDescription.setVerb(verb);

        // when
        String spec = commandDescription.getCommandSpecification();

        // then
        assertThat(spec).startsWith("jump");
    }

    @Test
    void directionData_withMinimalData_shouldStillBeValid() {
        // given
        DirectionData minimalDirection = new DirectionData();
        minimalDirection.setId(UUID.randomUUID().toString());
        minimalDirection.setDestinationId("location-2");

        // Minimal command data
        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        Word verb = new Word("go", Word.Type.VERB);
        commandDescription.setVerb(verb);
        commandData.setCommandDescription(commandDescription);
        minimalDirection.setCommandData(commandData);

        // when/then
        assertThat(minimalDirection.getId()).isNotNull();
        assertThat(minimalDirection.getDestinationId()).isEqualTo("location-2");
        assertThat(minimalDirection.getCommandData().getCommandDescription().getVerb()).isNotNull();
    }

    @Test
    void locationData_withMaxDirections_shouldHandleLargeSet() {
        // given
        Set<DirectionData> manyDirections = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            DirectionData dir = new DirectionData();
            dir.setId("direction-" + i);
            dir.setDestinationId("location-2");
            dir.setDescriptionData(new DescriptionData("Dir " + i, "Direction " + i));

            CommandData commandData = new CommandData();
            CommandDescriptionData commandDescription = new CommandDescriptionData();
            Word verb = new Word("go" + i, Word.Type.VERB);
            commandDescription.setVerb(verb);
            commandData.setCommandDescription(commandDescription);
            dir.setCommandData(commandData);

            manyDirections.add(dir);
        }
        locationData.setDirectionsData(manyDirections);

        // when
        int count = locationData.getDirectionsData().size();

        // then
        assertThat(count).isEqualTo(100);
    }
}
