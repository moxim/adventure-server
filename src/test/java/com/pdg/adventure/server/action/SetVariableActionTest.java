package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
class SetVariableActionTest {

    private final VariableProvider variableProvider = new VariableProvider();

    @Test
    void execute_setsVariableInProvider() {
        // given
        assertThat(variableProvider.get("score")).isEmpty();

        // when
        new SetVariableAction("score", 100, variableProvider).execute();

        // then
        assertThat(variableProvider.get("score")).isPresent();
        Variable variable = variableProvider.findVariableOrThrow("score");
        assertThat(variable.value()).isEqualTo(100);
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new SetVariableAction("lives", 3, variableProvider).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void execute_overwritesExistingVariable() {
        // given
        variableProvider.set("health", 50);

        // when
        new SetVariableAction("health", 75, variableProvider).execute();

        // then
        Variable variable = variableProvider.findVariableOrThrow("health");
        assertThat(variable.value()).isEqualTo(75);
    }

}
