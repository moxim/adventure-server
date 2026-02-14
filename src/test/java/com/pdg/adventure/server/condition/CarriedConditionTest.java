package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.Item;

class CarriedConditionTest {

    private GameContext gameContext = mock(GameContext.class);
    private DescriptionProvider thingDescription = new DescriptionProvider("thing");
    private Item item = new Item(thingDescription, true);
    private CarriedCondition sut = new CarriedCondition(item, gameContext);

    @Test
    void check() {
        Container mockPocket = mock(Container.class);
        when(gameContext.getPocket()).thenReturn(mockPocket);

        assertAll(
            () -> {
                // when item is carried (pocket contains the item)
                when(mockPocket.contains(item)).thenReturn(true);
                sut = new CarriedCondition(item, gameContext);
                // then
                assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
            },
            () -> {
                // when item is not carried (pocket does not contain the item)
                Item anotherItem = new Item(new DescriptionProvider("another thing"), true);
                when(mockPocket.contains(anotherItem)).thenReturn(false);
                sut = new CarriedCondition(anotherItem, gameContext);
                // then
                assertThat(sut.check().getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
            }
        );
    }

    @Test
    void getItem() {
        assertThat(sut.getItem()).isEqualTo(item);
    }
}
