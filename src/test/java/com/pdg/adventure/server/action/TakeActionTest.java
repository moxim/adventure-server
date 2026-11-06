package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class TakeActionTest {

    @Mock private Item item;
    @Mock private Container pocket;

    @Test
    void execute_delegatesToMoveItemAction_movingItemToPocket() {
        when(item.getParentContainer()).thenReturn(mock(Container.class));
        when(pocket.getSize()).thenReturn(0);
        when(pocket.getMaxSize()).thenReturn(10);
        when(item.getParentContainer().remove(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(pocket.add(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));

        ExecutionResult result = new TakeAction(item, new ContainerSupplier(pocket)).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(pocket).add(item);
    }

    @Test
    void execute_refusesWithSM27_whenThePocketHoldsAsManyObjectsAsItMay() {
        when(pocket.getSize()).thenReturn(3);
        when(pocket.getMaxSize()).thenReturn(3);

        ExecutionResult result = new TakeAction(item, new ContainerSupplier(pocket)).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM27.defaultText());
        verify(pocket, never()).add(any());
    }

    @Test
    void execute_refusesWithSM43_whenTheItemWouldMakeThePlayerCarryTooMuch() {
        when(pocket.getSize()).thenReturn(1);
        when(pocket.getMaxSize()).thenReturn(10);
        when(pocket.getCarriedWeight()).thenReturn(8);
        when(pocket.getMaxWeight()).thenReturn(10);
        when(item.getWeight()).thenReturn(3);
        when(item.getStrippedBasicDescription()).thenReturn("anvil");

        ExecutionResult result = new TakeAction(item, new ContainerSupplier(pocket)).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(SystemMessageKey.SM43.defaultText().formatted("anvil"));
        verify(pocket, never()).add(any());
    }
}
