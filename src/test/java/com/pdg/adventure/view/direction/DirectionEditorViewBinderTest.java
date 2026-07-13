package com.pdg.adventure.view.direction;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Tests for Vaadin Binder validation and binding logic in DirectionEditorView.
 * Focuses on field binding, validation rules, and data synchronization.
 */
@ExtendWith(MockitoExtension.class)
class DirectionEditorViewBinderTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private AdventureAccessService accessService;

    private DirectionEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private DirectionData directionData;
    private Binder<DirectionViewModel> binder;

    @BeforeEach
    void setUp() throws Exception {
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

        view = new DirectionEditorView(adventureService, accessService);
        locationData.getDirectionsData().add(directionData);

        // Access private binder field using reflection
        Field binderField = DirectionEditorView.class.getDeclaredField("binder");
        binderField.setAccessible(true);
        binder = (Binder<DirectionViewModel>) binderField.get(view);

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithDirectionId(String aDirectionId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                new RouteParam(RouteIds.DIRECTION_ID.getValue(), aDirectionId)));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    void binder_shouldBeCreatedInConstructor() {
        // then
        assertThat(binder).isNotNull();
    }

    @Test
    void binder_afterSetData_shouldHaveBean() {
        //given
        // when
        enterWithDirectionId("direction-1");

        // then
        assertThat(view.getViewModel()).isNotNull();
    }

    @Test
    void binder_withValidData_shouldPassValidation() {
        // given
        enterWithDirectionId("direction-1");

        // when
        BinderValidationStatus<DirectionViewModel> status = binder.validate();

        // then
        assertThat(status.isOk()).isTrue();
        assertThat(status.getValidationErrors()).isEmpty();
    }

    @Test
    void binder_withoutVerb_shouldFailValidation() throws Exception {
        // given
        enterWithDirectionId("direction-1");

        // Get the view model and remove verb
        DirectionViewModel viewModel = view.getViewModel();
        viewModel.setVerb(null);
        binder.readBean(viewModel);

        // when
        BinderValidationStatus<DirectionViewModel> status = binder.validate();

        // then
        assertThat(status.isOk()).isFalse();
        assertThat(status.getValidationErrors()).isNotEmpty();
    }

    @Test
    void binder_hasChanges_shouldReturnFalseAfterReadBean() {
        // given
        enterWithDirectionId("direction-1");

        // when
        DirectionViewModel viewModel = view.getViewModel();
        binder.readBean(viewModel);

        // then
        assertThat(binder.hasChanges()).isFalse();
    }

    @Test
    void binder_shouldBindDirectionId() {
        // given
        enterWithDirectionId("direction-1");

        // when
        DirectionViewModel viewModel = view.getViewModel();

        // then
        assertThat(viewModel).isNotNull();
        assertThat(viewModel.getId()).isEqualTo("direction-1");
    }

    @Test
    void binder_shouldBindDescriptions() {
        // given
        enterWithDirectionId("direction-1");

        // when
        DirectionViewModel viewModel = view.getViewModel();

        // then
        assertThat(viewModel.getShortDescription()).isEqualTo("Go north");
        assertThat(viewModel.getLongDescription()).isEqualTo("You can go north");
    }
}
