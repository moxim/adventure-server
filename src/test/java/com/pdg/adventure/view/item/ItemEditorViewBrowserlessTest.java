package com.pdg.adventure.view.item;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Browserless (UI-unit) tests for {@link ItemEditorView}, exercising the rendered component tree
 * with Vaadin's browserless test framework ({@code find()} queries and {@code test()} testers).
 *
 * <p>Focus is the "Commands" button added next to the "Is worn" checkbox: its presence, its
 * placement, and — crucially — that it is only enabled for an item that already exists in the
 * location's container. A new {@link ItemData} is born with a (ULID) id, so "is this an existing
 * item?" can only be answered by container membership; these tests pin that behaviour.
 */
class ItemEditorViewBrowserlessTest extends BrowserlessTest {

    private static final String ITEM_ID = "item-1";

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;

    private AdventureData adventureData;
    private LocationData locationData;
    private ItemData existingItem;
    private ItemEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);

        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));

        ItemContainerData itemContainer = new ItemContainerData("container-1");
        itemContainer.setItems(new ArrayList<>());
        locationData.setItemContainerData(itemContainer);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);

        VocabularyData vocabularyData = new VocabularyData();
        Word sword = new Word("sword", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        vocabularyData.setWords(List.of(sword, golden));
        adventureData.setVocabularyData(vocabularyData);

        // A fully-described item that callers may add to the container to represent an existing item.
        existingItem = new ItemData();
        existingItem.setId(ITEM_ID);
        DescriptionData descData = new DescriptionData();
        descData.setNoun(sword);
        descData.setAdjective(golden);
        descData.setShortDescription("A golden sword");
        descData.setLongDescription("A magnificent golden sword");
        existingItem.setDescriptionData(descData);

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new ItemEditorView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /** Simulate router navigation carrying (or omitting) an itemId, as the real app does. */
    private void enterWithItemId(String itemId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of(adventureData.getId()));
        when(params.get(RouteIds.LOCATION_ID.getValue())).thenReturn(Optional.of(locationData.getId()));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        when(params.get(RouteIds.ITEM_ID.getValue())).thenReturn(Optional.ofNullable(itemId));
        view.beforeEnter(event);
    }

    private Button commandsButton() {
        return find(Button.class, view).withText("Commands").single();
    }

    private Checkbox checkboxLabelled(String label) {
        return find(Checkbox.class, view).all().stream()
                                      .filter(cb -> label.equals(cb.getLabel()))
                                      .findFirst()
                                      .orElseThrow(() -> new AssertionError("No checkbox labelled: " + label));
    }

    @Test
    @DisplayName("The view renders exactly one 'Commands' button")
    void view_rendersCommandsButton() {
        view.setData(adventureData, locationData);

        assertThat(commandsButton()).isNotNull();
    }

    @Test
    @DisplayName("The 'Commands' button sits immediately to the right of the 'Is worn' checkbox")
    void commandsButton_sitsRightOfIsWornCheckbox() {
        view.setData(adventureData, locationData);

        Button commands = commandsButton();
        Checkbox worn = checkboxLabelled("Is worn");

        Component parent = commands.getParent().orElseThrow();
        assertThat(worn.getParent()).contains(parent);

        List<Component> row = parent.getChildren().toList();
        assertThat(row.indexOf(commands)).isEqualTo(row.indexOf(worn) + 1);
    }

    @Test
    @DisplayName("The 'Commands' button is disabled for a new (unsaved) item")
    void commandsButton_isDisabled_forNewItem() {
        // No itemId entered and nothing in the container -> brand-new item.
        view.setData(adventureData, locationData);

        assertThat(commandsButton().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("The 'Commands' button is enabled for an item that exists in the container")
    void commandsButton_isEnabled_forExistingItem() {
        locationData.getItemContainerData().getItems().add(existingItem);
        enterWithItemId(ITEM_ID);

        view.setData(adventureData, locationData);

        assertThat(commandsButton().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("The view renders the three item-property checkboxes")
    void view_rendersThreePropertyCheckboxes() {
        view.setData(adventureData, locationData);

        assertThat(find(Checkbox.class, view).all())
                .extracting(Checkbox::getLabel)
                .contains("Can be picked up / Is containable", "Is wearable", "Is worn");
    }

    @Test
    @DisplayName("The Save button is disabled immediately after loading data")
    void saveButton_isDisabled_afterSetData() {
        view.setData(adventureData, locationData);

        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Toggling a property checkbox marks the form dirty and enables Reset")
    void togglingCheckbox_enablesResetButton() {
        view.setData(adventureData, locationData);

        Button reset = find(Button.class, view).withText("Reset").single();
        assertThat(reset.isEnabled()).isFalse();

        test(checkboxLabelled("Is wearable")).click();

        assertThat(reset.isEnabled()).isTrue();
    }
}
