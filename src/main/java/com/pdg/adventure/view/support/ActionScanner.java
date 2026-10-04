package com.pdg.adventure.view.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.DescriptionData;

/**
 * Finds every action of an adventure together with where it lives. A command can hold actions in six
 * places: a location's own commands, its directions, items lying in a location (including items nested
 * in containers), items carried in the player's pocket, and the three workflow lists. The usage trackers
 * (a message or picture "is used where?") share this one traversal so none of them misses a place.
 */
public final class ActionScanner {

    private ActionScanner() {
    }

    /**
     * Where the command holding an action lives.
     *
     * @param source              what holds the command when that is not a location's own command list, e.g.
     *                            {@code Item 'brass key'} or {@code Arrival Process}; {@code null} for a
     *                            location's own command
     * @param locationId          the location the command belongs to or lies in; {@code null} when it has none
     * @param locationDescription that location's short description; may be {@code null}
     */
    public record Origin(String source, String locationId, String locationDescription) {
    }

    /**
     * One action found in an adventure.
     *
     * @param actionNumber         the action's 1-based position in its command
     * @param commandSpecification the trigger of the command (of the first command, for a command chain)
     */
    public record ScannedAction(ActionData action, int actionNumber, String commandSpecification, Origin origin) {
    }

    /**
     * All actions of the given type in the adventure.
     *
     * @return the actions found, empty when the adventure is {@code null}
     */
    public static <T extends ActionData> List<ScannedAction> scan(AdventureData adventureData, Class<T> actionType) {
        List<ScannedAction> found = new ArrayList<>();
        if (adventureData == null) {
            return found;
        }

        Set<ItemData> visitedItems = Collections.newSetFromMap(new IdentityHashMap<>());

        Map<String, LocationData> locations = adventureData.getLocationData();
        if (locations != null) {
            for (Map.Entry<String, LocationData> locationEntry : locations.entrySet()) {
                scanLocation(locationEntry.getKey(), locationEntry.getValue(), actionType, visitedItems, found);
            }
        }

        scanItems(adventureData.getPlayerPocket(), "in pocket", new Origin(null, null, null), actionType,
                  visitedItems, found);
        scanWorkflow(adventureData.getWorkflowData(), actionType, found);

        return found;
    }

    private static <T extends ActionData> void scanLocation(String locationId, LocationData location,
                                                            Class<T> actionType, Set<ItemData> visitedItems,
                                                            List<ScannedAction> found) {
        if (location == null) {
            return;
        }
        String locationDesc = location.getDescriptionData() != null ?
                              location.getDescriptionData().getShortDescription() : null;
        Origin origin = new Origin(null, locationId, locationDesc);

        scanCommandProvider(location.getCommandProviderData(), origin, actionType, found);

        if (location.getDirectionsData() != null) {
            for (DirectionData direction : location.getDirectionsData()) {
                scanDirection(direction, locationId, locationDesc, actionType, found);
            }
        }

        if (location.getItemContainerData() != null) {
            // The location's own container is only a holder - what it holds is what can carry commands.
            scanItemList(location.getItemContainerData().getItems(), null, origin, actionType, visitedItems, found);
        }
    }

    private static <T extends ActionData> void scanDirection(DirectionData direction, String locationId,
                                                             String locationDesc, Class<T> actionType,
                                                             List<ScannedAction> found) {
        if (direction == null || direction.getCommandData() == null) {
            return;
        }
        String name = describe(direction.getDescriptionData());
        if (name.isEmpty()) {
            name = specOf(direction.getCommandData());
        }
        scanCommand(direction.getCommandData(), specOf(direction.getCommandData()),
                    new Origin("Direction '" + name + "'", locationId, locationDesc), actionType, found);
    }

    /**
     * Scans an item container's items, and the items inside any nested container.
     *
     * @param container   the container whose items to scan; {@code null} is ignored
     * @param placeSuffix text appended to each item's label, e.g. {@code in pocket}; {@code null} for none
     */
    private static <T extends ActionData> void scanItems(ItemContainerData container, String placeSuffix,
                                                         Origin place, Class<T> actionType,
                                                         Set<ItemData> visitedItems, List<ScannedAction> found) {
        if (container != null) {
            scanItemList(container.getItems(), placeSuffix, place, actionType, visitedItems, found);
        }
    }

    private static <T extends ActionData> void scanItemList(List<ItemData> items, String placeSuffix, Origin place,
                                                            Class<T> actionType, Set<ItemData> visitedItems,
                                                            List<ScannedAction> found) {
        if (items == null) {
            return;
        }
        for (ItemData item : items) {
            // Items can be null when a @DBRef fails to resolve; the identity set guards against shared items.
            if (item == null || !visitedItems.add(item)) {
                continue;
            }
            String label = "Item '" + itemName(item) + "'" + (placeSuffix == null ? "" : " (" + placeSuffix + ")");
            scanCommandProvider(item.getCommandProviderData(),
                                new Origin(label, place.locationId(), place.locationDescription()), actionType, found);
            if (item instanceof ItemContainerData nested) {
                scanItemList(nested.getItems(), placeSuffix, place, actionType, visitedItems, found);
            }
        }
    }

    private static <T extends ActionData> void scanWorkflow(WorkflowData workflow, Class<T> actionType,
                                                            List<ScannedAction> found) {
        if (workflow == null) {
            return;
        }
        scanCommandList(workflow.getCommands(), "Workflow Process", actionType, found);
        scanCommandList(workflow.getInterceptorCommands(), "Response Process", actionType, found);
        scanCommandList(workflow.getArrivalProcesses(), "Arrival Process", actionType, found);
    }

    private static <T extends ActionData> void scanCommandList(List<CommandData> commands, String source,
                                                               Class<T> actionType, List<ScannedAction> found) {
        if (commands == null) {
            return;
        }
        for (CommandData command : commands) {
            scanCommand(command, specOf(command), new Origin(source, null, null), actionType, found);
        }
    }

    private static <T extends ActionData> void scanCommandProvider(CommandProviderData provider, Origin origin,
                                                                   Class<T> actionType, List<ScannedAction> found) {
        if (provider == null || provider.getAvailableCommands() == null) {
            return;
        }
        for (CommandChainData chain : provider.getAvailableCommands().values()) {
            if (chain != null && chain.getCommands() != null && !chain.getCommands().isEmpty()) {
                // A chain is listed under the trigger of its first command
                String commandSpec = specOf(chain.getCommands().getFirst());
                for (CommandData command : chain.getCommands()) {
                    scanCommand(command, commandSpec, origin, actionType, found);
                }
            }
        }
    }

    private static <T extends ActionData> void scanCommand(CommandData command, String commandSpec, Origin origin,
                                                           Class<T> actionType, List<ScannedAction> found) {
        if (command == null || command.getActions() == null) {
            return;
        }
        int actionNumber = 1;
        for (ActionData action : command.getActions()) {
            if (actionType.isInstance(action)) {
                found.add(new ScannedAction(action, actionNumber, commandSpec, origin));
            }
            actionNumber++;
        }
    }

    private static String specOf(CommandData command) {
        return command == null || command.getCommandDescription() == null
               ? null : command.getCommandDescription().getCommandSpecification();
    }

    private static String itemName(ItemData item) {
        String name = describe(item.getDescriptionData());
        return name.isEmpty() ? item.getId() : name;
    }

    private static String describe(DescriptionData descriptionData) {
        return descriptionData == null ? "" : ViewSupporter.getDescriptionText(descriptionData).trim();
    }
}
