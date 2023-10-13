package com.pdg.adventure.server.location;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.pdg.adventure.model.Word;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DirectionTest {

    private static final String GLOWING_TXT = "glowing";
    private static final String PORTAL_TXT = "portal";
    private final Vocabulary vocabulary = new Vocabulary();

    {
        vocabulary.addNewWord("enter", Word.Type.VERB);
    }

    private final Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);

    private final Location destination =
            new Location(new DescriptionProvider(GLOWING_TXT, PORTAL_TXT), pocket);
    private final GenericCommandDescription directionDescription = new GenericCommandDescription("enter", destination);
    private final GenericCommand moveCommand = new GenericCommand(directionDescription,
                                                                      new MovePlayerAction(destination,
                                                                                           Environment::setCurrentLocation,
                                                                                           new MessagesHolder()));
    private final GenericDirection sut = new GenericDirection(moveCommand, destination, true);

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
                                                                                                   Environment::setCurrentLocation,
                                                                                                   new MessagesHolder()));
        GenericDirection noAdj = new GenericDirection(moveCommand, destination, true);

        // when

        // then
        assertThat(noAdj.getShortDescription()).contains(PORTAL_TXT);
        assertThat(noAdj.getLongDescription()).isEqualTo("You may enter the portal.");
    }

    @Test
    void getDescriptionsWithoutLocation() throws Exception {
        // given
        GenericDirection noAdj = new GenericDirection(moveCommand, destination, false);

        // when

        // then
        assertThat(noAdj.getShortDescription()).isEqualTo("enter");
        assertThat(noAdj.getLongDescription()).isEqualTo("enter");
    }
}
