package com.pdg.adventure.server.action;

import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class DecrementVariableActionTest {
    private static final String varName = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private DecrementVariableAction sut = new DecrementVariableAction(varName, "1", variableProvider);

    @Test
    void executeWithAlphaString() {
        // given
        variableProvider.set(new Variable(varName, "wrongValue"));

        // when
        Throwable thrown = catchThrowable(() -> sut.execute());

        // then
        assertThat(thrown).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void executeWithNumericString() {
        // given
        variableProvider.set(new Variable(varName, "2"));

        // when
       sut.execute();

        // then
        assertThat(variableProvider.get(varName).aValue()).isEqualTo("1");
    }
}
