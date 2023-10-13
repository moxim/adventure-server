package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        ExecutionResult result = sut.add(new Item(descriptionProvider, false));

        // then
        assertThat(result.getResultMessage()).contains("can't put");
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
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));
        sut.add(new Item(descriptionProvider, true));

        // when
        ExecutionResult result = sut.add(new Item(descriptionProvider, true));

        // then
        assertThat(result.getResultMessage()).contains("already full");
    }

    @Test
    void cannotRemoveMissingItem() {
        // given

        // when
        ExecutionResult result = sut.remove(new Item(descriptionProvider, true));

        // then
        assertThat(result.getResultMessage()).contains("There is no");
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
        sut.add(item);

        // when
        ExecutionResult result = sut.add(item);

        // then
        assertThat(result.getResultMessage()).contains("already present");
    }
}
