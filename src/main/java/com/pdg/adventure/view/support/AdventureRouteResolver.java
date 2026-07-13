package com.pdg.adventure.view.support;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.ThingData;
import com.pdg.adventure.server.security.service.AdventureAccessService;

/**
 * Resolves domain objects from a navigation event's route parameters, access-checked
 * where applicable. Every method shows a "not found or access denied" notification
 * and returns {@code Optional.empty()} on failure; callers decide where to forward.
 */
public final class AdventureRouteResolver {

    private AdventureRouteResolver() {
    }

    public static Optional<AdventureData> resolveAdventure(BeforeEnterEvent event,
                                                             AdventureAccessService accessService) {
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isEmpty()) {
            return Optional.empty();
        }
        Optional<AdventureData> adventure = accessService.findAdventureById(adventureId.get(),
                ViewSupporter.getCurrentUser());
        if (adventure.isEmpty()) {
            showNotFound("Adventure", adventureId.get());
        }
        return adventure;
    }

    public static Optional<LocationData> resolveLocation(AdventureData adventure, BeforeEnterEvent event) {
        Optional<String> locationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (locationId.isEmpty()) {
            return Optional.empty();
        }
        LocationData location = adventure.getLocationData().get(locationId.get());
        if (location == null) {
            showNotFound("Location", locationId.get());
            return Optional.empty();
        }
        return Optional.of(location);
    }

    public static Optional<ItemData> resolveItem(LocationData location, BeforeEnterEvent event) {
        Optional<String> itemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (itemId.isEmpty()) {
            return Optional.empty();
        }
        Optional<ItemData> item = location.getItemContainerData().getItems().stream()
                .filter(candidate -> candidate.getId().equals(itemId.get()))
                .findFirst();
        if (item.isEmpty()) {
            showNotFound("Item", itemId.get());
        }
        return item;
    }

    public static Optional<DirectionData> resolveDirection(LocationData location, BeforeEnterEvent event) {
        Optional<String> directionId = event.getRouteParameters().get(RouteIds.DIRECTION_ID.getValue());
        if (directionId.isEmpty()) {
            return Optional.empty();
        }
        Optional<DirectionData> direction = location.getDirectionsData().stream()
                .filter(candidate -> candidate.getId().equals(directionId.get()))
                .findFirst();
        if (direction.isEmpty()) {
            showNotFound("Direction", directionId.get());
        }
        return direction;
    }

    public static Optional<MessageData> resolveMessage(AdventureData adventure, BeforeEnterEvent event) {
        Optional<String> messageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue());
        if (messageId.isEmpty()) {
            return Optional.empty();
        }
        MessageData message = adventure.getMessages().get(messageId.get());
        if (message == null) {
            showNotFound("Message", messageId.get());
            return Optional.empty();
        }
        return Optional.of(message);
    }

    public static Optional<CommandChainData> resolveCommandChain(ThingData thing, BeforeEnterEvent event) {
        Optional<String> commandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (commandId.isEmpty()) {
            return Optional.empty();
        }
        CommandChainData chain = thing.getCommandProviderData().getAvailableCommands().get(commandId.get());
        if (chain == null) {
            showNotFound("Command", commandId.get());
            return Optional.empty();
        }
        return Optional.of(chain);
    }

    /**
     * Decodes a route parameter that may or may not be percent-encoded.
     *
     * <p>Cold-load (bookmark/refresh) navigation delivers route parameters still
     * percent-encoded by the browser; in-app {@code navigate()} calls preserve the raw value
     * as originally constructed. Free-text ids (e.g. vocabulary-derived command specifications)
     * may legitimately contain {@code %} or {@code +} characters that were never encoded, so
     * this performs percent-only decoding (never form/{@code +}-to-space decoding) and falls
     * back to the raw value on malformed input (a bare {@code %} not followed by two hex
     * digits means the value was never encoded in the first place).
     *
     * @param raw the raw route parameter value
     * @return the percent-decoded value, or {@code raw} unchanged if it is not validly
     *         percent-encoded
     */
    public static String decodeRouteParam(String raw) {
        try {
            return UriUtils.decode(raw, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return raw;
        }
    }

    private static void showNotFound(String kind, String id) {
        Notification notification = Notification.show(
                kind + " not found or access denied: " + id, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
