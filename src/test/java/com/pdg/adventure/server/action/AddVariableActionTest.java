package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.support.VariableProvider;

class AddVariableActionTest {
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private final VariableProvider variableProvider = new VariableProvider();
    private final AddVariableAction sut = new AddVariableAction(FIRST, SECOND, variableProvider);

    @Test
    void execute_addsFirstToSecond_andLeavesFirstUnchanged() {
        variableProvider.set(FIRST, 4);
        variableProvider.set(SECOND, 10);

        ExecutionResult result = sut.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(variableProvider.get(SECOND).orElseThrow().value()).isEqualTo(14);
        assertThat(variableProvider.get(FIRST).orElseThrow().value()).isEqualTo(4);
    }

    @Test
    void execute_withSameVariableTwice_doublesIt() {
        variableProvider.set(FIRST, 3);

        new AddVariableAction(FIRST, FIRST, variableProvider).execute();

        assertThat(variableProvider.get(FIRST).orElseThrow().value()).isEqualTo(6);
    }

    @Test
    void execute_withUnknownFirst_throwsConfigurationException_andChangesNothing() {
        variableProvider.set(SECOND, 10);

        assertThatThrownBy(sut::execute).isInstanceOf(ConfigurationException.class).hasMessageContaining(FIRST);
        assertThat(variableProvider.get(SECOND).orElseThrow().value()).isEqualTo(10);
    }

    @Test
    void execute_withUnknownSecond_throwsConfigurationException_andDoesNotDefineIt() {
        variableProvider.set(FIRST, 4);

        assertThatThrownBy(sut::execute).isInstanceOf(ConfigurationException.class).hasMessageContaining(SECOND);
        assertThat(variableProvider.isDefined(SECOND)).isFalse();
    }
}
