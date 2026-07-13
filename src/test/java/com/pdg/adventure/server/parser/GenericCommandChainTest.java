package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

class GenericCommandChainTest {

    private static Action action(String message) {
        Action a = mock(Action.class);
        ExecutionResult r = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        r.setResultMessage(message);
        when(a.execute()).thenReturn(r);
        return a;
    }

    private static PreCondition precondition(ExecutionResult.State state) {
        PreCondition p = mock(PreCondition.class);
        when(p.check()).thenReturn(new CommandExecutionResult(state));
        return p;
    }

    /** A single-action command, optionally gated by one precondition. */
    private static GenericCommand command(PreCondition precond, String message) {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        if (precond != null) {
            cmd.addPreCondition(precond);
        }
        cmd.addAction(action(message));
        return cmd;
    }

    @Test
    void runsEveryApplicableCommand_andJoinsMessages() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(null, "one"));
        chain.addCommand(command(null, "two"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("one" + System.lineSeparator() + "two");
    }

    @Test
    void skipsCommandsWhosePreconditionsFail_runsTheRest() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(precondition(ExecutionResult.State.FAILURE), "suited"));   // skipped
        chain.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "no-suit"));  // runs
        chain.addCommand(command(null, "also-here"));                                        // always runs

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("no-suit" + System.lineSeparator() + "also-here");
    }

    @Test
    void whenNoCommandApplies_resultIsFailure() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(precondition(ExecutionResult.State.FAILURE), "never"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void blankMessagesAreSkippedWhenJoining() {
        GenericCommandChain chain = new GenericCommandChain();
        chain.addCommand(command(null, "shown"));
        chain.addCommand(command(null, ""));      // side-effect-only command
        chain.addCommand(command(null, "again"));

        ExecutionResult result = chain.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("shown" + System.lineSeparator() + "again");
    }

    @Test
    void jumpSeaScenario_noSuit_showsNoSuitAndAlsoHere() {
        // Player is NOT wearing the suit: WORN command is skipped,
        // NOT_WORN command applies, and the no-precondition command always applies.
        GenericCommandChain jumpSea = new GenericCommandChain();
        jumpSea.addCommand(command(precondition(ExecutionResult.State.FAILURE), "jump_sea_ok"));            // WORN: skipped
        jumpSea.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "jetty_jump_sea_no_suit")); // NOT_WORN
        jumpSea.addCommand(command(null, "jump_sea_also_here"));                                            // always

        ExecutionResult result = jumpSea.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("jetty_jump_sea_no_suit" + System.lineSeparator() + "jump_sea_also_here");
    }

    @Test
    void jumpSeaScenario_suitOn_showsOkAndAlsoHere() {
        // Player IS wearing the suit at the jetty: WORN command applies,
        // NOT_WORN is skipped, and the no-precondition command always applies.
        GenericCommandChain jumpSea = new GenericCommandChain();
        jumpSea.addCommand(command(precondition(ExecutionResult.State.SUCCESS), "jump_sea_ok"));            // WORN
        jumpSea.addCommand(command(precondition(ExecutionResult.State.FAILURE), "jetty_jump_sea_no_suit")); // NOT_WORN: skipped
        jumpSea.addCommand(command(null, "jump_sea_also_here"));                                            // always

        ExecutionResult result = jumpSea.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("jump_sea_ok" + System.lineSeparator() + "jump_sea_also_here");
    }
}
