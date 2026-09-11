package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class SetVariableActionTest {

    @Mock private VariableProvider variableProvider;

    @Test
    void execute_setsVariableInProvider() {
        new SetVariableAction("score", 100, variableProvider).execute();

        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(variableProvider).set(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("score");
        assertThat(captor.getValue().value()).isEqualTo(100);
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new SetVariableAction("lives", 3, variableProvider).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
