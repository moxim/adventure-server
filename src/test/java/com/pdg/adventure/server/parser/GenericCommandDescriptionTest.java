package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericCommandDescriptionTest {

    @Test
    void equals_ignoresPrepositionAndAdverb() {
        // The whole PrepositionCondition/AdverbCondition design relies on this: two commands
        // differing only in preposition/adverb must still be the SAME map key in
        // GenericCommandProvider/Workflow, so their Commands merge into one chain instead of
        // colliding as distinct entries.
        GenericCommandDescription a = new GenericCommandDescription("switch", "", "lamp", "on", "");
        GenericCommandDescription b = new GenericCommandDescription("switch", "", "lamp", "off", "");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void getPrepositionAndAdverb_defaultToEmpty_forShorterConstructors() {
        GenericCommandDescription description = new GenericCommandDescription("take", "sword");

        assertThat(description.getPreposition()).isEmpty();
        assertThat(description.getAdverb()).isEmpty();
    }

    @Test
    void getPrepositionAndAdverb_areCarriedByTheFiveArgConstructor() {
        GenericCommandDescription description =
                new GenericCommandDescription("switch", "", "lamp", "on", "slowly");

        assertThat(description.getPreposition()).isEqualTo("on");
        assertThat(description.getAdverb()).isEqualTo("slowly");
    }
}
