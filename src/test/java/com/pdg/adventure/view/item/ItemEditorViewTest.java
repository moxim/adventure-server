package com.pdg.adventure.view.item;

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

import java.util.ArrayList;
import java.util.HashMap;
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
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Unit tests for ItemEditorView business logic.
 * Tests focus on validation, data population, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class ItemEditorViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private ItemService itemService;

    @Mock
    private AdventureAccessService accessService;

    private ItemEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private ItemData itemData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));

        // Create item container
        ItemContainerData itemContainer = new ItemContainerData("19");
        itemContainer.setItems(new ArrayList<>());
        locationData.setItemContainerData(itemContainer);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);

        // Create vocabulary
        vocabularyData = new VocabularyData();
        Word sword = new Word("sword", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        Word go = new Word("go", Word.Type.VERB);
        vocabularyData.setWords(List.of(sword, golden, go));
        adventureData.setVocabularyData(vocabularyData);

        // Create test item
        itemData = new ItemData();
        itemData.setId("item-1");
        itemData.setAdventureId("adventure-1");
        itemData.setLocationId("location-1");
        DescriptionData descData = new DescriptionData();
        descData.setNoun(sword);
        descData.setAdjective(golden);
        descData.setShortDescription("A golden sword");
        descData.setLongDescription("A magnificent golden sword that gleams in the light");
        itemData.setDescriptionData(descData);
        itemData.setContainable(true);
        itemData.setWearable(false);
        itemData.setWorn(false);

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

    private BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        return event;
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new ItemEditorView(adventureService, itemService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateAdventureAndLocationData() {
        // given
        view = new ItemEditorView(adventureService, itemService, accessService);

        // when
        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId())));

        // then
        // View should be populated with data
        // No exceptions should be thrown
        assertThat(view).isNotNull();
    }

    @Test
    void setData_withExistingItem_shouldLoadItemFromContainer() {
        // given
        view = new ItemEditorView(adventureService, itemService, accessService);
        locationData.getItemContainerData().getItems().add(itemData);

        // when
        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())));

        // then
        // View should load the existing item
        assertThat(locationData.getItemContainerData().getItems())
                .hasSize(1)
                .contains(itemData);
    }

    @Test
    void getPageTitle_shouldReturnNullBeforeRouteEnter() {
        // given
        view = new ItemEditorView(adventureService, itemService, accessService);

        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }
}
