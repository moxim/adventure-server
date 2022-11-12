package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.AlreadyPresentException;
import com.pdg.adventure.server.exception.ContainerFullException;
import com.pdg.adventure.server.exception.NotContainableException;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContainerTest {
    private static final int CONTAINER_MAX_SIZE = 3;
    private static final DescriptionProvider descriptionProvider = new DescriptionProvider("box");
    private final Container sut = new GenericContainer(descriptionProvider, CONTAINER_MAX_SIZE);

    @Test
    void testCreation() {
        // given

        // when

        // then
        assertThat(sut.getContents()).isEmpty();
        assertThat(sut.getSize()).isZero();
        assertThat(sut.getMaxSize()).isEqualTo(CONTAINER_MAX_SIZE);
    }

    @Test
    void cannotPutAnyItemIntoContainer() {
        // given

        // when

        // then
        assertThatThrownBy(() ->
                sut.add(new Item(descriptionProvider, false))
        ).hasMessageContaining(NotContainableException.CANNOT_PUT_TEXT);
    }

    @Test
    void checkContainerMaxed() {
        // given

        // when
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));

        // then
        assertThat(sut.getSize()).isEqualTo(3);
    }

    @Test
    void cannotItemWhenContainerFull() {
        // given

        // when
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));

        // then
        assertThatThrownBy(() ->
                sut.add(new Item(descriptionProvider, true))
        ).hasMessageContaining(ContainerFullException.ALREADY_FULL_TEXT);
    }

    @Test
    void cannotRemoveMissingItem() {
        // given

        // when

        // then
        assertThat(TestSupporter.removeItemToBoolean(sut, new Item(descriptionProvider, false))).isFalse();
    }

    @Test
    void canRemovePresentItem() {
        // given
        Item item = new Item(descriptionProvider, true);

        // when
        sut.add(item);

        // then
        assertThat(TestSupporter.removeItemToBoolean(sut, item)).isTrue();
    }

    @Test
    void cannotAddPresentItem() {
        // given
        Item item = new Item(descriptionProvider, true);

        // when
        sut.add(item);

        // then
        assertThatThrownBy(() -> sut.add(item)).hasMessageContaining(AlreadyPresentException.ALREADY_PRESENT_TEXT);

    }
}
