package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;

class LessThanConditionTest {
    private static final String VAR_NAME = "t";
    private final VariableProvider variableProvider = new VariableProvider();
    private final LessThanCondition sut = new LessThanCondition(VAR_NAME, 2, variableProvider);

    @Test
    void testVariableMeetsCondition() {
        // given
        variableProvider.set(VAR_NAME, 1);

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void testVariableFailsCondition() {
        // given
        variableProvider.set(VAR_NAME, 3);

        // when

        // then
        assertThat(TestSupporter.conditionToBoolean(sut)).isFalse();
    }
}
