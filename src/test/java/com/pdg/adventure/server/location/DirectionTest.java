package com.pdg.adventure.server.location;

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
        vocabulary.addWord("south", Vocabulary.WordType.VERB);
    }

    private final Direction destination = new Direction("enter",
            new Location(new DescriptionProvider(GLOWING_TXT,
            PORTAL_TXT)), true, vocabulary);

    @Test
    void getDestination() {
        // given

        // when

        // then
        assertThat(destination.getDestination().getAdjective()).isEqualTo(GLOWING_TXT);
        assertThat(destination.getDestination().getNoun()).isEqualTo(PORTAL_TXT);
    }

    @Test
    void getAdjective() {
        // given

        // when

        // then
        assertThat(destination.getAdjective()).isEqualTo(GLOWING_TXT);
    }

    @Test
    void getNoun() {
        // given

        // when

        // then
        assertThat(destination.getNoun()).isEqualTo(PORTAL_TXT);
    }

    @Test
    void getShortDescription() {
        // given

        // when

        // then
        assertThat(destination.getShortDescription()).contains(GLOWING_TXT);
        assertThat(destination.getShortDescription()).contains(PORTAL_TXT);
    }

    @Test
    void getLongDescription() {
        // given

        // when

        // then
        assertThat(destination.getLongDescription()).isEqualTo("You can enter the glowing portal");
    }

    @Test
    void getLongDescriptionWithoutAdjective() {
        // given
        Direction noAdj = new Direction("enter", new Location(new DescriptionProvider(PORTAL_TXT)), true,
                vocabulary);

        // when

        // then
        assertThat(noAdj.getShortDescription()).contains(PORTAL_TXT);
        assertThat(noAdj.getLongDescription()).isEqualTo("You can enter the portal");
    }

    @Test
    void getDescriptionsWithoutLocation() throws Exception {
        // given
        Direction noAdj = new Direction("south", new Location(new DescriptionProvider(PORTAL_TXT)), false,
                vocabulary);

        // when

        // then
        assertThat(noAdj.getShortDescription()).isEqualTo("south");
        assertThat(noAdj.getLongDescription()).isEqualTo("south");
    }
}
