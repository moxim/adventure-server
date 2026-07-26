package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.testhelper.TestSupporter;

class ChanceConditionTest {

    @Test
    void succeedsWhenRollEqualsChance() {
        ChanceCondition sut = new ChanceCondition(50, () -> 50);

        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void succeedsWhenRollIsLowerThanChance() {
        ChanceCondition sut = new ChanceCondition(50, () -> 10);

        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void failsWhenRollIsGreaterThanChance() {
        ChanceCondition sut = new ChanceCondition(50, () -> 51);

        assertThat(TestSupporter.conditionToBoolean(sut)).isFalse();
    }

    @Test
    void hundredChanceAlwaysSucceeds() {
        ChanceCondition sut = new ChanceCondition(100, () -> 100);

        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }

    @Test
    void defaultConstructorUsesRandomRollWithinBounds() {
        ChanceCondition sut = new ChanceCondition(100);

        assertThat(TestSupporter.conditionToBoolean(sut)).isTrue();
    }
}
