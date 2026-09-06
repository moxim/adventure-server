package com.pdg.adventure.server.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class WorkflowTest {

    private final List<String> told = new ArrayList<>();
    private Workflow workflow;

    @BeforeEach
    void setUp() {
        GameContext gameContext = new GameContext();
        gameContext.setOutputSink(told::add);
        workflow = gameContext.setUpWorkflows();
    }

    @Test
    void preProcess_executesPreCommandsInAlphabeticalVerbOrder_regardlessOfInsertionOrder() {
        addPreCommand(new GenericCommandDescription("zoo"), "Zoo message.");
        addPreCommand(new GenericCommandDescription("apple"), "Apple message.");
        addPreCommand(new GenericCommandDescription("middle"), "Middle message.");

        workflow.preProcess();

        assertThat(told).containsExactly("Apple message.", "Middle message.", "Zoo message.");
    }

    // CommandFactory.setUpWorkflowCommands registers a sentinel preCommand keyed ("~", "~", "~")
    // whose action prints the turn prompt ("What now? > "; SM2) - it must keep coming after every
    // author-authored ambient message each turn, the way the old TreeMap iteration (sorted by the
    // "verb|adjective|noun" description string, where '|' and '~' both sort above lowercase
    // letters) already guaranteed. String.CASE_INSENSITIVE_ORDER must preserve that.
    @Test
    void preProcess_stillOrdersTheTurnPromptSentinelLast() {
        addPreCommand(new GenericCommandDescription("zebra"), "Zebra message.");
        addPreCommand(new GenericCommandDescription("apple"), "Apple message.");
        addPreCommand(new GenericCommandDescription("~", "~", "~"), SystemMessageKey.SM2.defaultText());

        workflow.preProcess();

        assertThat(told).containsExactly("Apple message.", "Zebra message.", SystemMessageKey.SM2.defaultText());
    }

    @Test
    void preProcess_ordersSameVerbCommandsByAdjectiveThenNoun() {
        addPreCommand(new GenericCommandDescription("look", "big", "door"), "Big door.");
        addPreCommand(new GenericCommandDescription("look", "big", "chest"), "Big chest.");
        addPreCommand(new GenericCommandDescription("look", "small", "box"), "Small box.");

        workflow.preProcess();

        assertThat(told).containsExactly("Big chest.", "Big door.", "Small box.");
    }

    private void addPreCommand(GenericCommandDescription aDescription, String aMessage) {
        Command command = mock(Command.class);
        when(command.execute()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS, aMessage));
        workflow.addPreCommand(aDescription, command);
    }
}
