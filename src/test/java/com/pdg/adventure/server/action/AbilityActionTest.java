package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

class AbilityActionTest {

    private GameContext gameContext;
    private GenericContainer pocket;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 10);
        gameContext.setPocket(pocket);
    }

    @Test
    void execute_setsBothLimitsOfThePocket_andSucceedsSilently() {
        ExecutionResult result = new AbilityAction(25, 4, gameContext).execute();

        assertThat(pocket.getMaxWeight()).isEqualTo(25);
        assertThat(pocket.getMaxSize()).isEqualTo(4);
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void execute_acceptsZero() {
        new AbilityAction(0, 0, gameContext).execute();

        assertThat(pocket.getMaxWeight()).isZero();
        assertThat(pocket.getMaxSize()).isZero();
    }

    @Test
    void execute_withAMissingValue_leavesThatLimitAlone() {
        new AbilityAction(null, 3, gameContext).execute();

        assertThat(pocket.getMaxWeight()).isEqualTo(Integer.MAX_VALUE);
        assertThat(pocket.getMaxSize()).isEqualTo(3);
    }

    @Test
    void execute_withoutAPocket_stillSucceeds() {
        gameContext.setPocket(null);

        assertThat(new AbilityAction(1, 1, gameContext).execute().getExecutionState())
                .isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isNotInformationalOnly_becauseItChangesTheGame() {
        assertThat(new AbilityAction(1, 1, gameContext).isInformationalOnly()).isFalse();
    }
}
