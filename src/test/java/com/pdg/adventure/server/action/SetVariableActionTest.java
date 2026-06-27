package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class SetVariableActionTest {

    @Mock private VariableProvider variableProvider;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_setsVariableInProvider() {
        new SetVariableAction("score", "100", variableProvider, messagesHolder).execute();

        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(variableProvider).set(captor.capture());
        assertThat(captor.getValue().aName()).isEqualTo("score");
        assertThat(captor.getValue().aValue()).isEqualTo("100");
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new SetVariableAction("lives", "3", variableProvider, messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
