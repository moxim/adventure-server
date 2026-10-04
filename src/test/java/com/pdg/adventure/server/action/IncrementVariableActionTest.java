package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.support.VariableProvider;

class IncrementVariableActionTest {
    private static final String VAR_NAME = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private final IncrementVariableAction sut = new IncrementVariableAction(VAR_NAME, 1, variableProvider);

    @Test
    void executeWithNumericString() {
        // given
        variableProvider.set(VAR_NAME, 2);

        // when
        sut.execute();

        // then
        assertThat(variableProvider.get(VAR_NAME).get().value()).isEqualTo(3);
    }
}
