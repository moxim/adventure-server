package com.pdg.adventure.server.location;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;


class DirectionTest {

    private static final String GLOWING_TXT = "glowing";
    private static final String PORTAL_TXT = "portal";
    private final Map<String, Location> allLocations = new HashMap<>();
    private final Vocabulary vocabulary = new Vocabulary();
    private final Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);
    private final Location destination = new Location(new DescriptionProvider(GLOWING_TXT, PORTAL_TXT), pocket);

    {
        vocabulary.createNewWord("enter", Word.Type.VERB);
        allLocations.put(destination.getId(), destination);
    }


    private final GenericCommandDescription directionDescription = new GenericCommandDescription("enter", destination);
    private final GenericCommand moveCommand = new GenericCommand(directionDescription,
                                                                      new MovePlayerAction(destination,
                                                                                           new MessagesHolder()));
    private final GenericDirection sut = new GenericDirection(allLocations, moveCommand, destination.getId(), true);

    @Test
    void getDestination() {
        // given

        // when

        // then
        assertThat(sut.getAdjective()).isEqualTo(GLOWING_TXT);
        assertThat(sut.getNoun()).isEqualTo(PORTAL_TXT);
    }

    @Test
    void getAdjective() {
        // given

        // when

        // then
        assertThat(sut.getAdjective()).isEqualTo(GLOWING_TXT);
    }

    @Test
    void getNoun() {
        // given

        // when

        // then
        assertThat(sut.getNoun()).isEqualTo(PORTAL_TXT);
    }

    @Test
    void getShortDescription() {
        // given

        // when

        // then
        assertThat(sut.getShortDescription()).contains(GLOWING_TXT);
        assertThat(sut.getShortDescription()).contains(PORTAL_TXT);
    }

    @Test
    void getLongDescription() {
        // given

        // when

        // then
        assertThat(sut.getLongDescription()).isEqualTo("You may enter the glowing portal.");
    }

    @Test
    void getLongDescriptionWithoutAdjective() {
        // given
        Location destination = new Location(new DescriptionProvider(PORTAL_TXT), pocket);
        GenericCommandDescription directionDescription = new GenericCommandDescription("enter", destination);
        GenericCommand moveCommand = new GenericCommand(directionDescription, new MovePlayerAction(destination,
                                                                                                   new MessagesHolder()));
        allLocations.clear();
        allLocations.put(destination.getId(), destination);
        GenericDirection noAdj = new GenericDirection(allLocations, moveCommand, destination.getId(), true);

        // when

        // then
        assertThat(noAdj.getShortDescription()).contains(PORTAL_TXT);
        assertThat(noAdj.getLongDescription()).isEqualTo("You may enter the portal.");
    }

    @Test
    void getDescriptionsWithoutLocation() throws Exception {
        // given
        GenericDirection noAdj = new GenericDirection(allLocations, moveCommand, destination.getId(), false);

        // when

        // then
        assertThat(noAdj.getShortDescription()).isEqualTo("enter");
        assertThat(noAdj.getLongDescription()).isEqualTo("enter");
    }
}
