package com.pdg.adventure.server.location;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.testhelper.TestSupporter;

class LocationTest {

    private Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);
    Map<String, Location> allLocations = new HashMap<>();
    private final Location sut = new Location(new DescriptionProvider("small", "perch"), pocket);
    {
        allLocations.put(sut.getId(), sut);
    }
    private final Direction direction = new GenericDirection(allLocations,
                                                             new GenericCommand(
                                                                     new GenericCommandDescription("loop"), null), sut.getId());
    private final Item mouse = new Item(new DescriptionProvider("mouse"), true);

    @Test
    void addItem() {
        // given

        // when
        boolean success = TestSupporter.addItemToBoolean(sut.getContainer(), mouse);

        // then
        assertThat(success).isTrue();
        assertThat(mouse.getParentContainer()).isEqualTo(sut.getContainer());
        assertThat(sut.contains(mouse)).isTrue();
    }

    @Test
    void removeItemFailsIfItemIsNotFound() throws Exception {
        // given
        assertThat(sut.contains(mouse)).isFalse();

        // when
        boolean success = TestSupporter.removeItemToBoolean(sut.getContainer(), mouse);

        // then
        assertThat(success).isFalse();
        assertThat(sut.contains(mouse)).isFalse();
    }

    @Test
    void removeItemSucceedsIfItemIsFound() throws Exception {
        // given
        sut.addItem(mouse);

        // when
        boolean success = TestSupporter.removeItemToBoolean(sut.getContainer(), mouse);

        // then
        assertThat(success).isTrue();
        assertThat(sut.contains(mouse)).isFalse();
    }

    @Test
    void addDirection() {
    }

    @Test
    void applyCommand() {
    }

    @Test
    void getLongDescription() {
        // given
        sut.addItem(mouse);
        sut.addDirection(direction);

        // when
        String desc = sut.getLongDescription();

        // then
        assertThat(desc).contains(List.of("small", "perch", "mouse", "loop"));
    }
}
