package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;

class CommandHandlerExamineFallbackTest {

    private static final String EXAMINE_VERB = "examine";
    private static final String FALLBACK_DESCRIPTION = "A rusty sword.";

    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        commandHandler = new CommandHandler();
    }

    @Test
    void fallback_firesWhenNoExplicitCommandMatchesExamineVerb() {
        commandHandler.setExamineFallback(EXAMINE_VERB, () -> FALLBACK_DESCRIPTION);

        ExecutionResult result = commandHandler.applyCommand(new GenericCommandDescription(EXAMINE_VERB, "sword"));

        assertThat(result.hasCommandMatched()).isTrue();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo(FALLBACK_DESCRIPTION);
    }

    @Test
    void fallback_doesNotFireForOtherVerbs() {
        commandHandler.setExamineFallback(EXAMINE_VERB, () -> FALLBACK_DESCRIPTION);

        ExecutionResult result = commandHandler.applyCommand(new GenericCommandDescription("look", "sword"));

        assertThat(result.hasCommandMatched()).isFalse();
    }

    @Test
    void fallback_doesNotFireWhenNotSet() {
        ExecutionResult result = commandHandler.applyCommand(new GenericCommandDescription(EXAMINE_VERB, "sword"));

        assertThat(result.hasCommandMatched()).isFalse();
    }

    @Test
    void explicitCommand_suppressesFallback() {
        commandHandler.setExamineFallback(EXAMINE_VERB, () -> FALLBACK_DESCRIPTION);

        GenericCommandDescription desc = new GenericCommandDescription(EXAMINE_VERB, "sword");
        commandHandler.addCommand(new GenericCommand(desc,
                new MessageAction("Custom examine response.", new MessagesHolder())));

        ExecutionResult result = commandHandler.applyCommand(desc);

        assertThat(result.hasCommandMatched()).isTrue();
        assertThat(result.getResultMessage()).isEqualTo("Custom examine response.");
    }

    @Test
    void getMatchingCommandChain_returnsFallbackChainWhenNoExplicitMatch() {
        commandHandler.setExamineFallback(EXAMINE_VERB, () -> FALLBACK_DESCRIPTION);

        var chains = commandHandler.getMatchingCommandChain(new GenericCommandDescription(EXAMINE_VERB, "sword"));

        assertThat(chains).hasSize(1);
        assertThat(chains.getFirst().execute().getResultMessage()).isEqualTo(FALLBACK_DESCRIPTION);
    }

    @Test
    void getMatchingCommandChain_returnsEmptyWhenFallbackNotSetAndNoExplicitMatch() {
        var chains = commandHandler.getMatchingCommandChain(new GenericCommandDescription(EXAMINE_VERB, "sword"));

        assertThat(chains).isEmpty();
    }
}
