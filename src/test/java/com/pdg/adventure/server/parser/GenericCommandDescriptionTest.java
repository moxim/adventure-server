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

    @Test
    void equals_ignoresNoun2AndAdjective2() {
        GenericCommandDescription a =
                new GenericCommandDescription("use", "", "spanner", "on", "", "ancient", "machine");
        GenericCommandDescription b =
                new GenericCommandDescription("use", "", "spanner", "on", "", "rusty", "engine");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void getNoun2AndAdjective2_defaultToEmpty_forShorterConstructors() {
        GenericCommandDescription description = new GenericCommandDescription("take", "sword");

        assertThat(description.getNoun2()).isEmpty();
        assertThat(description.getAdjective2()).isEmpty();
    }

    @Test
    void getNoun2AndAdjective2_areCarriedByTheSevenArgConstructor() {
        GenericCommandDescription description =
                new GenericCommandDescription("use", "", "spanner", "on", "", "ancient", "machine");

        assertThat(description.getAdjective2()).isEqualTo("ancient");
        assertThat(description.getNoun2()).isEqualTo("machine");
    }
}
