package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

class GameStateSnapshotterTest {

    private GameContext gameContext;
    private VariableProvider variables;
    private GameStateSnapshotter snapshotter;

    private GenericContainer pocket;
    private GenericContainer hallItems;
    private GenericContainer cellarItems;
    private Location hall;
    private Location cellar;
    private Item apple;
    private Item lamp;
    private Item cloak;
    private Item sword;
    private Item ring;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        hallItems = new GenericContainer(new DescriptionProvider("hall items"), 10);
        cellarItems = new GenericContainer(new DescriptionProvider("cellar items"), 10);
        gameContext.setPocket(pocket);
        hall = new Location(new DescriptionProvider("great", "hall"), hallItems);
        cellar = new Location(new DescriptionProvider("dark", "cellar"), cellarItems);
        gameContext.setCurrentLocation(hall);

        apple = new Item(new DescriptionProvider("red", "apple"), true);
        lamp = new Item(new DescriptionProvider("brass", "lamp"), true);
        cloak = new Item(new DescriptionProvider("grey", "cloak"), true);
        sword = new Item(new DescriptionProvider("rusty", "sword"), true);
        ring = new Item(new DescriptionProvider("gold", "ring"), true);
        hallItems.add(apple);
        hallItems.add(lamp);
        lamp.setLight(50);
        pocket.add(cloak);
        cloak.setIsWorn(true);
        cellarItems.add(sword);
        // the ring is known to the game but lies in no container yet ("nowhere")

        Map<String, Item> items = new HashMap<>();
        for (Item item : List.of(apple, lamp, cloak, sword, ring)) {
            items.put(item.getId(), item);
        }
        Map<String, Container> containers = new HashMap<>();
        for (GenericContainer container : List.of(pocket, hallItems, cellarItems)) {
            containers.put(container.getId(), container);
        }
        Map<String, Location> locations = new HashMap<>();
        locations.put(hall.getId(), hall);
        locations.put(cellar.getId(), cellar);
        variables = new VariableProvider();
        variables.set("score", 7);

        AdventureConfig config = mock(AdventureConfig.class);
        when(config.allItems()).thenReturn(items);
        when(config.allContainers()).thenReturn(containers);
        when(config.allLocations()).thenReturn(locations);
        when(config.allVariables()).thenReturn(variables);
        snapshotter = new GameStateSnapshotter(gameContext, config);
    }

    @Test
    void capture_recordsLocationPictureKnownItemsAndOrderedContainers() {
        hall.setTimesVisited(3);
        gameContext.setCurrentPictureId("pic-1");

        GameSnapshotData snapshot = snapshotter.capture();

        assertThat(snapshot.getCurrentLocationId()).isEqualTo(hall.getId());
        assertThat(snapshot.getCurrentPictureId()).isEqualTo("pic-1");
        assertThat(snapshot.getKnownItemIds())
                .containsExactlyInAnyOrder(apple.getId(), lamp.getId(), cloak.getId(), sword.getId(), ring.getId());
        assertThat(snapshot.getContainers().get(hallItems.getId())).containsExactly(apple.getId(), lamp.getId());
        assertThat(snapshot.getContainers().get(pocket.getId())).containsExactly(cloak.getId());
        assertThat(snapshot.getWornItemIds()).containsExactly(cloak.getId());
        assertThat(snapshot.getLumen()).containsOnly(Map.entry(lamp.getId(), 50));
        assertThat(snapshot.getVisits()).containsEntry(hall.getId(), 3);
        assertThat(snapshot.getVariables()).containsEntry("score", 7);
    }

    @Test
    void capture_countsAnItemInNoContainerAsKnownButUnplaced() {
        GameSnapshotData snapshot = snapshotter.capture();

        assertThat(snapshot.getKnownItemIds()).contains(ring.getId());
        assertThat(snapshot.getContainers().values()).noneMatch(ids -> ids.contains(ring.getId()));
    }

    @Test
    void restore_putsEverythingBackAsCaptured() {
        hall.setTimesVisited(3);
        GameSnapshotData snapshot = snapshotter.capture();

        // play on: take the apple, drop the cloak into the cellar and take it off, move, change the world
        hallItems.remove(apple);
        pocket.add(apple);
        pocket.remove(cloak);
        cloak.setIsWorn(false);
        cellarItems.add(cloak);
        lamp.setLight(0);
        hall.setTimesVisited(9);
        variables.set("score", 99);
        gameContext.setCurrentLocation(cellar);
        gameContext.setCurrentPictureId("other");

        boolean restored = snapshotter.restore(snapshot);

        assertThat(restored).isTrue();
        assertThat(hallItems.getContents()).containsExactly(apple, lamp);
        assertThat(pocket.getContents()).containsExactly(cloak);
        assertThat(cellarItems.getContents()).containsExactly(sword);
        assertThat(apple.getParentContainer()).isSameAs(hallItems);
        assertThat(cloak.getParentContainer()).isSameAs(pocket);
        assertThat(cloak.isWorn()).isTrue();
        assertThat(lamp.getLight()).isEqualTo(50);
        assertThat(hall.getTimesVisited()).isEqualTo(3);
        assertThat(variables.get("score").orElseThrow().value()).isEqualTo(7);
        assertThat(gameContext.getCurrentLocation()).isSameAs(hall);
        assertThat(gameContext.getCurrentPictureId()).isNull();
    }

    @Test
    void restore_bringsBackAnItemDestroyedAfterTheSave() {
        GameSnapshotData snapshot = snapshotter.capture();
        // what DestroyAction does: remove from the container, leave getParentContainer() stale
        apple.getParentContainer().remove(apple);

        snapshotter.restore(snapshot);

        assertThat(hallItems.getContents()).containsExactly(apple, lamp);
    }

    @Test
    void restore_removesAnItemThatWasNowhereAtSaveTime() {
        GameSnapshotData snapshot = snapshotter.capture();
        hallItems.add(ring);

        snapshotter.restore(snapshot);

        assertThat(hallItems.getContents()).containsExactly(apple, lamp);
        assertThat(hallItems.getContents()).doesNotContain(ring);
    }

    @Test
    void restore_leavesItemsTheSaveNeverKnewAlone() {
        GameSnapshotData snapshot = snapshotter.capture();
        Item newKey = new Item(new DescriptionProvider("silver", "key"), true);
        hallItems.add(newKey);

        snapshotter.restore(snapshot);

        assertThat(hallItems.getContents()).containsExactly(apple, lamp, newKey);
    }

    @Test
    void restore_withAMissingSavedLocation_changesNothing() {
        GameSnapshotData snapshot = snapshotter.capture();
        snapshot.setCurrentLocationId("gone");
        hallItems.remove(apple);
        pocket.add(apple);

        boolean restored = snapshotter.restore(snapshot);

        assertThat(restored).isFalse();
        assertThat(pocket.getContents()).contains(apple);
        assertThat(gameContext.getCurrentLocation()).isSameAs(hall);
    }

    @Test
    void restore_skipsIdsThatNoLongerExist_andRestoresTheRest() {
        GameSnapshotData snapshot = snapshotter.capture();
        snapshot.getContainers().put("vanished-container", List.of(apple.getId()));
        snapshot.getContainers().get(hallItems.getId()).add("vanished-item");
        hallItems.remove(apple);
        pocket.add(apple);

        boolean restored = snapshotter.restore(snapshot);

        assertThat(restored).isTrue();
        assertThat(hallItems.getContents()).containsExactly(apple, lamp);
    }
}
