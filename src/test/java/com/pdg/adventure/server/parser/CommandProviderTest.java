package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.*;

class CommandProviderTest {

    @Test
    void addingCommandWithSameDescriptionCreatesChain() {
        // given
        CommandProvider provider = new CommandProvider();
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
        CommandProvider provider = new CommandProvider();
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
        CommandProvider provider = new CommandProvider();
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
}
