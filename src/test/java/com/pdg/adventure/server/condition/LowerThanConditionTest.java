package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class LowerThanConditionTest {
    private static final String varName = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private LowerThanCondition sut = new LowerThanCondition(varName, 2, variableProvider);

    @Test
    void testVariableMeetsCondition() throws Exception {
        // given
        variableProvider.set(new Variable(varName, "1"));

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void executeWithAlphaString() {
        // given
        variableProvider.set(new Variable(varName, "wrongValue"));

        // when
        Throwable thrown = catchThrowable(() -> sut.check());

        // then
        assertThat(thrown).isInstanceOf(ConfigurationException.class);
    }

    @Test
    void testVariableFailsCondition() throws Exception {
        // given
        variableProvider.set(new Variable(varName, "3"));

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isFalse();
    }
}
