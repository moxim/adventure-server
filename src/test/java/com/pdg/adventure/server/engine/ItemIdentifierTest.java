package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.AmbiguousCommandException;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
        Containable item = ItemIdentifier.findItem(container, commandDescription);

        // then
        assertThat(item).isEqualTo(ring);
    }

    @Test
    void findFullyQualifiedItem() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "small", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getAdjective(), commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        Containable item = ItemIdentifier.findItem(container, commandDescription);

        // then
        assertThat(item).isEqualTo(ring);
    }

    @Test
    void failToFindItem() {
        // given
        GenericCommandDescription commandDescription = new GenericCommandDescription("", "small", "ring");
        DescriptionProvider ringDescription = new DescriptionProvider(commandDescription.getNoun());
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        Throwable thrown = catchThrowable(() -> ItemIdentifier.findItem(container, commandDescription));

        // then
        assertThat(thrown).isInstanceOf(ItemNotFoundException.class);
        assertThat(thrown.getMessage()).contains("not found");

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
        Throwable thrown = catchThrowable( () -> ItemIdentifier.findItem(container, commandDescription));

        // then
        assertThat(thrown).isInstanceOf(AmbiguousCommandException.class);
        assertThat(thrown.getMessage()).contains("Too many");
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
        Throwable thrown = catchThrowable( () -> ItemIdentifier.findItem(container, commandDescription));

        // then
        assertThat(thrown).isInstanceOf(AmbiguousCommandException.class);
        assertThat(thrown.getMessage()).contains("Too many");
    }
    
    
}
