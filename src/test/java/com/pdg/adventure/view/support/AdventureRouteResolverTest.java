package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.item.ItemsMenuView;
import com.pdg.adventure.view.location.LocationsMenuView;

class AdventureRouteResolverTest extends BrowserlessTest {

    private AdventureAccessService accessService;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
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

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    // --- resolveAdventure ---

    @Test
    void resolveAdventure_validId_returnsAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).contains(adventure);
    }

    @Test
    void resolveAdventure_unknownId_returnsEmptyAndShowsNotification() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void resolveAdventure_missingRouteParam_returnsEmptyWithoutCallingAccessService() {
        BeforeEnterEvent event = eventWithParams();

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventure(event, accessService);

        assertThat(result).isEmpty();
        verify(accessService, never()).findAdventureById(any(), any());
    }

    // --- resolveLocation ---

    @Test
    void resolveLocation_validId_returnsLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setLocationData(Map.of("loc-1", location));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocation(adventure, event);

        assertThat(result).contains(location);
    }

    @Test
    void resolveLocation_unknownId_returnsEmptyAndShowsNotification() {
        AdventureData adventure = new AdventureData();
        adventure.setLocationData(Map.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocation(adventure, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Location not found or access denied: missing");
    }

    // --- resolveItem ---

    @Test
    void resolveItem_validId_returnsItem() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setItemContainerData(container);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItem(location, event);

        assertThat(result).contains(item);
    }

    @Test
    void resolveItem_unknownId_returnsEmptyAndShowsNotification() {
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of());
        LocationData location = new LocationData();
        location.setItemContainerData(container);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItem(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Item not found or access denied: missing");
    }

    // --- resolveDirection ---

    @Test
    void resolveDirection_validId_returnsDirection() {
        DirectionData direction = new DirectionData();
        direction.setId("dir-1");
        LocationData location = new LocationData();
        location.setDirectionsData(Set.of(direction));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.DIRECTION_ID.getValue(), "dir-1"));

        Optional<DirectionData> result = AdventureRouteResolver.resolveDirection(location, event);

        assertThat(result).contains(direction);
    }

    @Test
    void resolveDirection_unknownId_returnsEmptyAndShowsNotification() {
        LocationData location = new LocationData();
        location.setDirectionsData(Set.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.DIRECTION_ID.getValue(), "missing"));

        Optional<DirectionData> result = AdventureRouteResolver.resolveDirection(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Direction not found or access denied: missing");
    }

    // --- resolveMessage ---

    @Test
    void resolveMessage_validId_returnsMessage() {
        MessageData message = new MessageData();
        AdventureData adventure = new AdventureData();
        adventure.setMessages(Map.of("msg-1", message));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1"));

        Optional<MessageData> result = AdventureRouteResolver.resolveMessage(adventure, event);

        assertThat(result).contains(message);
    }

    @Test
    void resolveMessage_unknownId_returnsEmptyAndShowsNotification() {
        AdventureData adventure = new AdventureData();
        adventure.setMessages(Map.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.MESSAGE_ID.getValue(), "missing"));

        Optional<MessageData> result = AdventureRouteResolver.resolveMessage(adventure, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Message not found or access denied: missing");
    }

    // --- resolveCommandChain ---

    @Test
    void resolveCommandChain_validId_returnsChain() {
        CommandChainData chain = new CommandChainData();
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of("go|north|", chain));
        LocationData location = new LocationData();
        location.setCommandProviderData(provider);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.COMMAND_ID.getValue(), "go|north|"));

        Optional<CommandChainData> result = AdventureRouteResolver.resolveCommandChain(location, event);

        assertThat(result).contains(chain);
    }

    @Test
    void resolveCommandChain_unknownId_returnsEmptyAndShowsNotification() {
        CommandProviderData provider = new CommandProviderData();
        provider.setAvailableCommands(Map.of());
        LocationData location = new LocationData();
        location.setCommandProviderData(provider);
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.COMMAND_ID.getValue(), "missing"));

        Optional<CommandChainData> result = AdventureRouteResolver.resolveCommandChain(location, event);

        assertThat(result).isEmpty();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Command not found or access denied: missing");
    }

    // --- resolveAdventureOrForward ---

    @Test
    void resolveAdventureOrForward_validId_returnsAdventureWithoutForwarding() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);

        assertThat(result).contains(adventure);
        verify(event, never()).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void resolveAdventureOrForward_unknownId_returnsEmptyAndForwardsToAdventuresMenu() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"));

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);

        assertThat(result).isEmpty();
        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void resolveAdventureOrForward_missingRouteParam_returnsEmptyAndForwardsToAdventuresMenu() {
        BeforeEnterEvent event = eventWithParams();

        Optional<AdventureData> result = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);

        assertThat(result).isEmpty();
        verify(event).forwardTo(AdventuresMenuView.class);
    }

    // --- resolveLocationOrForward ---

    @Test
    void resolveLocationOrForward_validId_returnsLocationWithoutForwarding() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of("loc-1", location));
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocationOrForward(adventure, event);

        assertThat(result).contains(location);
        verify(event, never()).forwardTo(eq(LocationsMenuView.class), any(RouteParameters.class));
    }

    @Test
    void resolveLocationOrForward_unknownId_returnsEmptyAndForwardsToLocationsMenuForAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setLocationData(Map.of());
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.LOCATION_ID.getValue(), "missing"));

        Optional<LocationData> result = AdventureRouteResolver.resolveLocationOrForward(adventure, event);

        assertThat(result).isEmpty();
        verify(event).forwardTo(LocationsMenuView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
    }

    // --- resolveItemOrForward ---

    @Test
    void resolveItemOrForward_validId_returnsItemWithoutForwarding() {
        ItemData item = new ItemData();
        item.setId("item-1");
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of(item));
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "item-1"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItemOrForward(adventure, location, event);

        assertThat(result).contains(item);
        verify(event, never()).forwardTo(eq(ItemsMenuView.class), any(RouteParameters.class));
    }

    @Test
    void resolveItemOrForward_unknownId_returnsEmptyAndForwardsToItemsMenuForLocation() {
        ItemContainerData container = new ItemContainerData("loc-1");
        container.setItems(List.of());
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setItemContainerData(container);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        BeforeEnterEvent event = eventWithParams(new RouteParam(RouteIds.ITEM_ID.getValue(), "missing"));

        Optional<ItemData> result = AdventureRouteResolver.resolveItemOrForward(adventure, location, event);

        assertThat(result).isEmpty();
        verify(event).forwardTo(ItemsMenuView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), "loc-1")));
    }
}
