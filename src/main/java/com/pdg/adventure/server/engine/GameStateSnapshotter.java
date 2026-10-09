package com.pdg.adventure.server.engine;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.Item;

/**
 * Captures the changing part of a running game and puts it back in place. The adventure itself (the authored
 * world) is never part of a snapshot. Works on the current browser session's state through the scoped proxies.
 */
@Component
public class GameStateSnapshotter {

    private final GameContext gameContext;
    private final AdventureConfig adventureConfig;

    public GameStateSnapshotter(GameContext aGameContext, @Lazy AdventureConfig anAdventureConfig) {
        gameContext = aGameContext;
        adventureConfig = anAdventureConfig;
    }

    public GameSnapshotData capture() {
        GameSnapshotData snapshot = new GameSnapshotData();
        Location current = gameContext.getCurrentLocation();
        snapshot.setCurrentLocationId(current == null ? null : current.getId());
        snapshot.setCurrentPictureId(gameContext.getCurrentPictureId());

        for (Item item : adventureConfig.allItems().values()) {
            snapshot.getKnownItemIds().add(item.getId());
            if (item.isWorn()) {
                snapshot.getWornItemIds().add(item.getId());
            }
            if (item.getLight() != 0) {
                snapshot.getLumen().put(item.getId(), item.getLight());
            }
        }
        // Placement is read from the containers: DestroyAction leaves an item's parent pointer stale.
        for (Map.Entry<String, Container> entry : adventureConfig.allContainers().entrySet()) {
            List<String> ids = snapshot.containerContents(entry.getKey());
            for (Containable containable : entry.getValue().getContents()) {
                if (containable instanceof Item item) {
                    ids.add(item.getId());
                }
            }
        }
        adventureConfig.allLocations().forEach((id, location) -> snapshot.getVisits().put(id, location.getTimesVisited()));
        snapshot.getVariables().putAll(adventureConfig.allVariables().getAllAsMap());
        return snapshot;
    }

    /**
     * Applies the snapshot to the current world, best-effort: ids that no longer exist are skipped and anything the
     * snapshot does not mention is left alone.
     *
     * @return false, with nothing changed, if the saved location no longer exists
     */
    public boolean restore(GameSnapshotData aSnapshot) {
        Map<String, Location> locations = adventureConfig.allLocations();
        Location savedLocation = locations.get(aSnapshot.getCurrentLocationId());
        if (savedLocation == null) {
            return false;
        }
        Map<String, Item> items = adventureConfig.allItems();
        Map<String, Container> containers = adventureConfig.allContainers();

        considerMovedItems(aSnapshot, items);

        considerContainerContents(aSnapshot, containers, items);

        considerItemStatesVisitsAndVariables(aSnapshot, items, locations);

        gameContext.setCurrentLocation(savedLocation);
        gameContext.setCurrentPictureId(aSnapshot.getCurrentPictureId());
        return true;
    }

    private static void considerMovedItems(final GameSnapshotData aSnapshot, final Map<String, Item> items) {
        for (String itemId : aSnapshot.getKnownItemIds()) {
            Item item = items.get(itemId);
            if (item != null && item.getParentContainer() != null) {
                item.getParentContainer().remove(item);
            }
        }
    }

    private static void considerContainerContents(final GameSnapshotData aSnapshot, final Map<String, Container> containers,
                                  final Map<String, Item> items) {
        aSnapshot.getContainers().forEach((containerId, itemIds) -> {
            Container container = containers.get(containerId);
            if (container == null) {
                return;
            }
            List<Containable> contents = new ArrayList<>();
            for (String itemId : itemIds) {
                Item item = items.get(itemId);
                if (item != null) {
                    contents.add(item);
                }
            }
            contents.addAll(container.getContents());
            container.setContents(contents);
            contents.forEach(containable -> containable.setParentContainer(container));
        });
    }

    private void considerItemStatesVisitsAndVariables(final GameSnapshotData aSnapshot, final Map<String, Item> items,
                                                      final Map<String, Location> locations) {
        for (String itemId : aSnapshot.getKnownItemIds()) {
            Item item = items.get(itemId);
            if (item != null) {
                item.setIsWorn(aSnapshot.getWornItemIds().contains(itemId));
                item.setLight(aSnapshot.getLumen().getOrDefault(itemId, 0));
            }
        }
        aSnapshot.getVisits().forEach((locationId, visits) -> {
            Location location = locations.get(locationId);
            if (location != null) {
                location.setTimesVisited(visits);
            }
        });
        VariableProvider variableProvider = adventureConfig.allVariables();
        aSnapshot.getVariables().forEach(variableProvider::set);
    }
}
