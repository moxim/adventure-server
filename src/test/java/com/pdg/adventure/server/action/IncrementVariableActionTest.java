package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

class IncrementVariableActionTest {
    private static final String varName = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private IncrementVariableAction sut = new IncrementVariableAction(varName, "1", variableProvider,
                                                                      new MessagesHolder());

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
        assertThat(variableProvider.get(varName).aValue()).isEqualTo("3");
    }
}
