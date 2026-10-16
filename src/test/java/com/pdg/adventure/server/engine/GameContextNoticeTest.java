package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

class GameContextNoticeTest {

    private GameContext gameContext;
    private GenericContainer pocket;
    private GenericContainer roomItems;
    private Location room;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        roomItems = new GenericContainer(new DescriptionProvider("room items"), 10);
        room = new Location(new DescriptionProvider("throne", "room"), roomItems);
        gameContext.setPocket(pocket);
        gameContext.setCurrentLocation(room);
    }

    @Test
    void playerNoticesChangeIn_theItemContainerOfTheCurrentLocation() {
        assertThat(gameContext.playerNoticesChangeIn(roomItems)).isTrue();
    }

    @Test
    void playerNoticesChangeIn_thePocket() {
        assertThat(gameContext.playerNoticesChangeIn(pocket)).isTrue();
    }

    @Test
    void playerNoticesChangeIn_theItemContainerOfAnotherLocation_isFalse() {
        GenericContainer elsewhereItems = new GenericContainer(new DescriptionProvider("other items"), 10);
        new Location(new DescriptionProvider("cellar"), elsewhereItems);

        assertThat(gameContext.playerNoticesChangeIn(elsewhereItems)).isFalse();
    }

    @Test
    void playerNoticesChangeIn_aChestInTheRoom_isFalse() {
        GenericContainer chest = new GenericContainer(new DescriptionProvider("chest"), 5);
        chest.setParentContainer(roomItems);

        assertThat(gameContext.playerNoticesChangeIn(chest)).isFalse();
    }

    @Test
    void playerNoticesChangeIn_aBagInThePocket_isFalse() {
        GenericContainer bag = new GenericContainer(new DescriptionProvider("bag"), 5);
        bag.setParentContainer(pocket);

        assertThat(gameContext.playerNoticesChangeIn(bag)).isFalse();
    }

    @Test
    void playerNoticesChangeIn_followsThePlayerToAnotherLocation() {
        GenericContainer cellarItems = new GenericContainer(new DescriptionProvider("cellar items"), 10);
        Location cellar = new Location(new DescriptionProvider("cellar"), cellarItems);

        gameContext.setCurrentLocation(cellar);

        assertThat(gameContext.playerNoticesChangeIn(cellarItems)).isTrue();
        assertThat(gameContext.playerNoticesChangeIn(roomItems)).isFalse();
    }

    @Test
    void playerNoticesChangeIn_inTheDark_theRoomIsNotSeenButThePocketIsFelt() {
        room.setLight(0);

        assertThat(gameContext.playerNoticesChangeIn(roomItems)).isFalse();
        assertThat(gameContext.playerNoticesChangeIn(pocket)).isTrue();
    }

    @Test
    void playerNoticesChangeIn_withoutAContainerOrALocation_isFalse() {
        assertThat(gameContext.playerNoticesChangeIn(null)).isFalse();

        gameContext.setCurrentLocation(null);
        assertThat(gameContext.playerNoticesChangeIn(roomItems)).isFalse();
    }
}
