package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ItemIdentifierTest {

    private DescriptionProvider descriptionProvider = new DescriptionProvider("simple", "container");
    private final Container container = new GenericContainer(descriptionProvider, 3);

    @Test
    void findItem() {
        // given
        String adjective = "";
        String noun = "ring";
        DescriptionProvider ringDescription = new DescriptionProvider(noun);
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        Containable item = ItemIdentifier.findItem(container, adjective, noun);

        // then
        assertThat(item).isEqualTo(ring);
    }

    @Test
    void findFullyQualifiedItem() {
        // given
        String adjective = "small";
        String noun = "ring";
        DescriptionProvider ringDescription = new DescriptionProvider(adjective, noun);
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        Containable item = ItemIdentifier.findItem(container, adjective, noun);

        // then
        assertThat(item).isEqualTo(ring);
    }

    @Test
    void failToFindItem() {
        // given
        String adjective = "small";
        String noun = "ring";
        DescriptionProvider ringDescription = new DescriptionProvider(noun);
        Item ring = new Item(ringDescription, true);
        container.add(ring);

        // when
        Throwable thrown = catchThrowable(() -> ItemIdentifier.findItem(container, adjective, noun));

        // then
        assertThat(thrown).isInstanceOf(ItemNotFoundException.class);
        assertThat(thrown.getMessage()).contains("not found");

    }

    @Test
    void ambiguousItems() {
        // given
        String adjective = "";
        String noun = "ring";
        DescriptionProvider ringDescription = new DescriptionProvider(noun);
        Item ring = new Item(ringDescription, true);
        container.add(ring);
        Item largeRing = new Item(ringDescription, true);
        container.add(largeRing);

        // when
        Throwable thrown = catchThrowable( () -> ItemIdentifier.findItem(container, adjective, noun));

        // then
        assertThat(thrown).isInstanceOf(ItemNotFoundException.class);
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

        // when
        Throwable thrown = catchThrowable( () -> ItemIdentifier.findItem(container, "", noun));

        // then
        assertThat(thrown).isInstanceOf(ItemNotFoundException.class);
        assertThat(thrown.getMessage()).contains("Too many");
    }
    
    
}
