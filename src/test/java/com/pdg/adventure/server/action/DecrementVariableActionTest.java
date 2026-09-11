package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

class DecrementVariableActionTest {
    private static final String VAR_NAME = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private final DecrementVariableAction sut = new DecrementVariableAction(VAR_NAME, 1, variableProvider);

    @Test
    void executeWithNumericString() {
        // given
        variableProvider.set(new Variable(VAR_NAME, 2));

        // when
        sut.execute();

        // then
        assertThat(variableProvider.get(VAR_NAME).value()).isEqualTo(1);
    }
}
