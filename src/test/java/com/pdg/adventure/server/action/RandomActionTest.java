package com.pdg.adventure.server.action;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

class RandomActionTest {
    private final VariableProvider variableProvider = new VariableProvider();

    @Test
    void execute_withAnUnknownVariable_createsItWithoutAnException() {
        ExecutionResult result = new RandomAction("dice", variableProvider).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(variableProvider.isDefined("dice")).isTrue();
    }

    @Test
    void execute_withAnExistingVariable_replacesItsValue() {
        variableProvider.set("dice", 500);

        new RandomAction("dice", variableProvider, bound -> 42).execute();

        assertThat(variableProvider.get("dice")).hasValueSatisfying(v -> assertThat(v.value()).isEqualTo(42));
    }

    @Test
    void execute_asksForAValueBelow100_soZeroAndNinetyNineAreBothPossible() {
        new RandomAction("low", variableProvider, bound -> {
            assertThat(bound).isEqualTo(100);
            return 0;
        }).execute();
        new RandomAction("high", variableProvider, bound -> bound - 1).execute();

        assertThat(variableProvider.get("low")).hasValueSatisfying(v -> assertThat(v.value()).isZero());
        assertThat(variableProvider.get("high")).hasValueSatisfying(v -> assertThat(v.value()).isEqualTo(99));
    }

    @RepeatedTest(50)
    void execute_withTheRealRandom_staysWithinZeroAndNinetyNine() {
        new RandomAction("dice", variableProvider).execute();

        assertThat(variableProvider.get("dice").orElseThrow().value()).isBetween(0, 99);
    }

    @Test
    void execute_returnsNoText() {
        ExecutionResult result = new RandomAction("dice", variableProvider).execute();

        assertThat(result.getResultMessage()).isNullOrEmpty();
    }
}
