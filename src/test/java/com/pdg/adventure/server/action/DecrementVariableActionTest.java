package com.pdg.adventure.server.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.jupiter.api.Test;

import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

class DecrementVariableActionTest {
    private static final String varName = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private DecrementVariableAction sut = new DecrementVariableAction(varName, "1", variableProvider, new MessagesHolder());

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
