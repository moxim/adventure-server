package com.pdg.adventure.server.tangible;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class ItemIdentifierTest {

    private final Container container = new GenericContainer(new DescriptionProvider("simple", "container"), 3);

    @Test
    void findItem() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        List<Containable> items = ItemIdentifier.findItems(container, commandDescription);

        // then
        assertThat(items).contains(ring);
    }

    @Test
    void findFullyQualifiedItem() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "small", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getAdjective(), commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        List<Containable> items = ItemIdentifier.findItems(container, commandDescription);

        // then
        assertThat(items).contains(ring);
    }

    @Test
    void failToFindItem() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "small", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        List<Containable> items = ItemIdentifier.findItems(container, commandDescription);

        // then
        assertThat(items).isEmpty();

    }

    @Test
    void ambiguousItems() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);
        Item largeRing = new Item(ringDescription, true);
        container.add(largeRing);

        // when
        List<Containable> items = ItemIdentifier.findItems(container, commandDescription);

        // then
        assertThat(items).size().isGreaterThan(1);
    }

    @Test
    void ambiguousQualifiedItems() {
        // given
        String noun = "ring";
        Item ring = new Item(new DescriptionProvider("small", noun), true);
        container.add(ring);
        Item largeRing = new Item(new DescriptionProvider("large", noun), true);
        container.add(largeRing);
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "", noun);

        // when
        List<Containable> items = ItemIdentifier.findItems(container, commandDescription);

        // then
        assertThat(items).size().isGreaterThan(1);
    }
    
    
}
