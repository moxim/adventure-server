package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.*;

class GenericCommandProviderTest {

    @Test
    void addingCommandWithSameDescriptionCreatesChain() {
        // given
        GenericCommandProvider provider = new GenericCommandProvider();
        GenericCommandDescription description = new GenericCommandDescription("open", "wooden", "door");

        Action action1 = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.FAILURE, "First action failed");
            }

            @Override
            public String getActionName() {
                return "FirstAction";
            }
        };

        Action action2 = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS, "Second action succeeded");
            }

            @Override
            public String getActionName() {
                return "SecondAction";
            }
        };

        Command command1 = new GenericCommand(description, action1);
        Command command2 = new GenericCommand(description, action2);

        // when
        provider.addCommand(command1);
        provider.addCommand(command2);

        // then
        assertThat(provider.getCommands()).hasSize(2);
        assertThat(provider.getAvailableCommands()).hasSize(1);

        CommandChain chain = provider.getAvailableCommands().get(description);
        assertThat(chain).isNotNull();
        assertThat(chain.getCommands()).hasSize(2);
        assertThat(chain.getCommands()).containsExactly(command1, command2);
    }

    @Test
    void commandChainExecutesUntilSuccessfulPreconditionsMet() {
        // given
        GenericCommandProvider provider = new GenericCommandProvider();
        GenericCommandDescription description = new GenericCommandDescription("open", "wooden", "door");

        // First command with failing precondition
        PreCondition failingPrecondition = new PreCondition() {
            @Override
            public String getId() {
                return "1";
            }

            @Override
            public void setId(final String anId) {

            }

            @Override
            public ExecutionResult check() {
                return new CommandExecutionResult(ExecutionResult.State.FAILURE, "Key not found");
            }

            @Override
            public String getName() {
                return "HasKey";
            }
        };

        Action action1 = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS, "Opened with key");
            }

            @Override
            public String getActionName() {
                return "OpenWithKey";
            }
        };

        // Second command with succeeding precondition
        PreCondition succeedingPrecondition = new PreCondition() {
            @Override
            public String getId() {
                return "";
            }

            @Override
            public void setId(final String anId) {

            }

            @Override
            public ExecutionResult check() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
            }

            @Override
            public String getName() {
                return "Always";
            }
        };

        Action action2 = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS, "Forced open");
            }

            @Override
            public String getActionName() {
                return "ForceOpen";
            }
        };

        Command command1 = new GenericCommand(description, action1);
        command1.addPreCondition(failingPrecondition);

        Command command2 = new GenericCommand(description, action2);
        command2.addPreCondition(succeedingPrecondition);

        // when
        provider.addCommand(command1);
        provider.addCommand(command2);
        ExecutionResult result = provider.applyCommand(description);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("Forced open");
    }

    @Test
    void addingDifferentCommandsCreatesMultipleChains() {
        // given
        GenericCommandProvider provider = new GenericCommandProvider();
        GenericCommandDescription openDoor = new GenericCommandDescription("open", "wooden", "door");
        GenericCommandDescription closeDoor = new GenericCommandDescription("close", "wooden", "door");

        Action action = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
            }

            @Override
            public String getActionName() {
                return "TestAction";
            }
        };

        Command command1 = new GenericCommand(openDoor, action);
        Command command2 = new GenericCommand(closeDoor, action);

        // when
        provider.addCommand(command1);
        provider.addCommand(command2);

        // then
        assertThat(provider.getCommands()).hasSize(2);
        assertThat(provider.getAvailableCommands()).hasSize(2);
    }

    @Test
    void matchingMatrixBehaviour() {
        GenericCommandProvider provider = new GenericCommandProvider();

        // dummy action that always succeeds
        Action successAction = new Action() {
            @Override
            public ExecutionResult execute() {
                return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
            }

            @Override
            public String getActionName() {
                return "noop";
            }
        };

        // create item descriptions: verbs {EMPTY, "take", "oil"} x adjectives {EMPTY, "rusty"}
        List<GenericCommandDescription> items = new ArrayList<>();
        String noun = "sword";
        String EMPTY = com.pdg.adventure.model.VocabularyData.EMPTY_STRING;

        items.add(new GenericCommandDescription(EMPTY, EMPTY, noun));
        items.add(new GenericCommandDescription(EMPTY, "rusty", noun));
        items.add(new GenericCommandDescription("take", EMPTY, noun));
        items.add(new GenericCommandDescription("take", "rusty", noun));
        items.add(new GenericCommandDescription("oil", EMPTY, noun));
        items.add(new GenericCommandDescription("oil", "rusty", noun));

        // add commands for each item
        for (GenericCommandDescription d : items) {
            provider.addCommand(new GenericCommand(d, successAction));
        }

        // helper to test a single input against an item
        java.util.function.BiFunction<GenericCommandDescription, GenericCommandDescription, Boolean> shouldMatch = (in, it) -> {
            // noun must match exactly
            if (!java.util.Objects.equals(in.getNoun(), it.getNoun())) return false;
            String inVerb = in.getVerb() == null ? EMPTY : in.getVerb();
            String inAdj = in.getAdjective() == null ? EMPTY : in.getAdjective();
            String itVerb = it.getVerb() == null ? EMPTY : it.getVerb();
            String itAdj = it.getAdjective() == null ? EMPTY : it.getAdjective();

            boolean verbMatches = itVerb.equals(inVerb) || EMPTY.equals(itVerb) || EMPTY.equals(inVerb);
            boolean adjMatches = itAdj.equals(inAdj) || EMPTY.equals(itAdj) || EMPTY.equals(inAdj);
            return verbMatches && adjMatches;
        };

        // inputs to test (from the user's matrix)
        List<GenericCommandDescription> inputs = List.of(
                new GenericCommandDescription("take", "rusty", noun),
                new GenericCommandDescription("take", EMPTY, noun),
                new GenericCommandDescription(EMPTY, "rusty", noun),
                new GenericCommandDescription(EMPTY, EMPTY, noun),
                new GenericCommandDescription("take", "short", noun),
                new GenericCommandDescription(EMPTY, "short", noun)
        );

        for (GenericCommandDescription in : inputs) {
            List<CommandChain> matches = provider.getMatchingCommandChain(in);
            for (GenericCommandDescription it : items) {
                CommandChain expectedChain = provider.getAvailableCommands().get(it);
                boolean expect = shouldMatch.apply(in, it);
                if (expect) {
                    assertThat(matches).as("expected match for input %s and item %s", in, it).contains(expectedChain);
                } else {
                    assertThat(matches).as("expected NO match for input %s and item %s", in, it).doesNotContain(expectedChain);
                }
            }
        }
    }
}
