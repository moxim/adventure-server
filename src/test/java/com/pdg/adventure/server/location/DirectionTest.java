package com.pdg.adventure.server.location;

import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.DirectionCommand;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DirectionTest {

    private static final String GLOWING_TXT = "glowing";
    private static final String PORTAL_TXT = "portal";
    private final Vocabulary vocabulary = new Vocabulary();
    {
        vocabulary.addWord("enter", Vocabulary.WordType.VERB);
    }
    private final Location destination = new Location(new DescriptionProvider(GLOWING_TXT, PORTAL_TXT));
    private final CommandDescription directionDescription = new CommandDescription("enter", destination);
    private final DirectionCommand moveCommand = new DirectionCommand(directionDescription, destination);
    private final GenericDirection sut = new GenericDirection(moveCommand, true);

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
        Location destination = new Location(new DescriptionProvider(PORTAL_TXT));
        CommandDescription directionDescription = new CommandDescription("enter", destination);
        DirectionCommand moveCommand = new DirectionCommand(directionDescription, destination);
        GenericDirection noAdj = new GenericDirection(moveCommand, true);

        // when

        // then
        assertThat(noAdj.getShortDescription()).contains(PORTAL_TXT);
        assertThat(noAdj.getLongDescription()).isEqualTo("You may enter the portal.");
    }

    @Test
    void getDescriptionsWithoutLocation() throws Exception {
        // given
        GenericDirection noAdj = new GenericDirection(moveCommand, false);

        // when

        // then
        assertThat(noAdj.getShortDescription()).isEqualTo("enter");
        assertThat(noAdj.getLongDescription()).isEqualTo("enter");
    }
}
