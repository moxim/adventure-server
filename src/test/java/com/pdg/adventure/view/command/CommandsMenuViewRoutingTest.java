package com.pdg.adventure.view.command;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class CommandsMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private ItemService itemService;
    private AdventureAccessService accessService;
    private CommandsMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        itemService = mock(ItemService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new CommandsMenuView(adventureService, itemService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    private static CommandProviderData providerWithOneCommand() {
        CommandData command = new CommandData();
        command.setCommandDescription(new CommandDescriptionData("go|north|"));
        CommandChainData chain = new CommandChainData();
        chain.setCommands(List.of(command));
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("go|north|", chain));
        return provider;
    }

    @Test
    void beforeEnter_locationScoped_validIds_populatesGridFromLocationCommands() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.getDescriptionData().setShortDescription("the dunes");
        location.setCommandProviderData(providerWithOneCommand());
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        assertThat(view.getPageTitle()).isEqualTo("Commands for the dunes");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void beforeEnter_locationScoped_gridShowsCommandsInAlphabeticalVerbOrder_regardlessOfMapOrder() {
        CommandData zebraCommand = new CommandData();
        zebraCommand.setCommandDescription(new CommandDescriptionData("zebra||"));
        CommandChainData zebraChain = new CommandChainData();
        zebraChain.setCommands(List.of(zebraCommand));

        CommandData appleCommand = new CommandData();
        appleCommand.setCommandDescription(new CommandDescriptionData("apple||"));
        CommandChainData appleChain = new CommandChainData();
        appleChain.setCommands(List.of(appleCommand));

        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("zebra||", zebraChain, "apple||", appleChain));

        LocationData location = new LocationData();
        location.setId("loc-1");
        location.getDescriptionData().setShortDescription("the dunes");
        location.setCommandProviderData(provider);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));

        Grid<CommandData> grid = (Grid<CommandData>) (Grid<?>) find(Grid.class, view).single();
        var gridTester = test(grid);
        assertThat(List.of(gridTester.getRow(0), gridTester.getRow(1)))
                .extracting(cmd -> cmd.getCommandDescription().getSafeVerb().getText())
                .containsExactly("apple", "zebra");
    }

    @Test
    void beforeEnter_itemScoped_validIds_populatesGridFromItemCommands() {
        ItemData item = new ItemData();
        item.setId("item-1");
        item.getDescriptionData().setShortDescription("a rusty key");
        item.setCommandProviderData(providerWithOneCommand());
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1")));

        assertThat(view.getPageTitle()).isEqualTo("Commands for a rusty key");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_unknownLocationId_forwardsToLocationsMenuViewForThatAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(LocationsMenuView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
    }

    @Test
    void beforeEnter_unknownItemId_forwardsToItemsMenuViewForThatLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(new ItemContainerData("loc-1"));
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"),
                new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        view.beforeEnter(event);

        verify(event).forwardTo(ItemsMenuView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Item not found or access denied: missing");
    }

    // Delete goes through a GridContextMenu item click, which a browserless test can't drive
    // reliably, so these build the confirmation dialog directly (package-private for this) and
    // drive it as a user would: open, then confirm or cancel.
    private LocationData enterLocationWithOneCommand() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.getDescriptionData().setShortDescription("the dunes");
        // Mutable collections (unlike providerWithOneCommand): deleting removes from them.
        CommandData command = new CommandData();
        command.setCommandDescription(new CommandDescriptionData("go|north|"));
        CommandChainData chain = new CommandChainData();
        chain.setCommands(new ArrayList<>(List.of(command)));
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(new HashMap<>(Map.of("go|north|", chain)));
        location.setCommandProviderData(provider);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));
        return location;
    }

    private static CommandData onlyCommandOf(LocationData aLocation) {
        return aLocation.getCommandProviderData().getAvailableCommands().values().iterator().next()
                        .getCommands().getFirst();
    }

    @Test
    void deleteConfirmDialog_asksFirst_andDeletesNothingUntilConfirmed() {
        LocationData location = enterLocationWithOneCommand();
        CommandData command = onlyCommandOf(location);

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(command);
        UI.getCurrent().add(dialog);
        dialog.open();

        assertThat(test(dialog).getHeader()).isEqualTo("Delete Command");
        assertThat(test(dialog).getText()).contains("go north");
        verify(adventureService, never()).saveLocationData(any());
        assertThat(location.getCommandProviderData().findChainIdContaining(command)).isPresent();
    }

    @Test
    void confirmingDeleteDialog_removesTheCommandAndPersistsTheLocation() {
        LocationData location = enterLocationWithOneCommand();
        CommandData command = onlyCommandOf(location);

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(command);
        UI.getCurrent().add(dialog);
        dialog.open();
        test(dialog).confirm();

        assertThat(location.getCommandProviderData().getAvailableCommands()).isEmpty();
        verify(adventureService).saveLocationData(location);
    }

    @Test
    void cancelingDeleteDialog_deletesNothing() {
        LocationData location = enterLocationWithOneCommand();
        CommandData command = onlyCommandOf(location);

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(command);
        UI.getCurrent().add(dialog);
        dialog.open();
        test(dialog).cancel();

        assertThat(location.getCommandProviderData().findChainIdContaining(command)).isPresent();
        verify(adventureService, never()).saveLocationData(any());
    }
}
