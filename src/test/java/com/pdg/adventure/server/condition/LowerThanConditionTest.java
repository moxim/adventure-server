package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;

class LowerThanConditionTest {
    private static final String varName = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private final LowerThanCondition sut = new LowerThanCondition(varName, 2, variableProvider);

    @Test
    void testVariableMeetsCondition() {
        // given
        variableProvider.set(new Variable(varName, 1));

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void testVariableFailsCondition() {
        // given
        variableProvider.set(new Variable(varName, 3));

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isFalse();
    }
}
