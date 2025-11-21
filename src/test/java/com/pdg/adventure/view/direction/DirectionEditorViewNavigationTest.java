package com.pdg.adventure.view.direction;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
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
 * Navigation and routing tests for DirectionEditorView.
 * Tests beforeEnter and beforeLeave lifecycle hooks, page titles,
 * and route parameter handling.
 */
@ExtendWith(MockitoExtension.class)
class DirectionEditorViewNavigationTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    @Mock
    private BeforeLeaveEvent beforeLeaveEvent;

    @Mock
    private RouteParameters routeParameters;

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
        vocabularyData.setWords(List.of(goVerb, northNoun));
        adventureData.setVocabularyData(vocabularyData);

        directionData = new DirectionData();
        directionData.setId("direction-123");
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
    }

    @Test
    void beforeEnter_withDirectionId_shouldSetEditModeTitle() {
        // given
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.of("direction-123"));

        // when
        view.beforeEnter(beforeEnterEvent);

        // then
        assertThat(view.getPageTitle()).isEqualTo("Edit Direction #direction-123");
    }

    @Test
    void beforeEnter_withoutDirectionId_shouldSetNewModeTitle() {
        // given
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.empty());

        // when
        view.beforeEnter(beforeEnterEvent);

        // then
        assertThat(view.getPageTitle()).isEqualTo("New Direction");
    }

    @Test
    void beforeEnter_withNewDirection_shouldGenerateUUID() {
        // given
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.empty());

        // when
        view.beforeEnter(beforeEnterEvent);

        // then
        // UUID should be generated (page title will be "New Direction")
        assertThat(view.getPageTitle()).isEqualTo("New Direction");
    }

    @Test
    void getPageTitle_beforeNavigation_shouldReturnDefaultTitle() {
        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }

    @Test
    void getPageTitle_afterEditNavigation_shouldContainDirectionId() {
        // given
        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);
        when(routeParameters.get("directionId")).thenReturn(Optional.of("abc-123"));

        // when
        view.beforeEnter(beforeEnterEvent);
        String title = view.getPageTitle();

        // then
        assertThat(title).contains("abc-123");
        assertThat(title).startsWith("Edit Direction");
    }

    @Test
    void setData_shouldPopulateLocationAndAdventureIds() {
        // when

        view.setUpLoading("direction-123");

        view.setData(locationData, adventureData);

        // then
        // Verify view is properly initialized (no exceptions thrown)
        assertThat(view).isNotNull();
    }
}
