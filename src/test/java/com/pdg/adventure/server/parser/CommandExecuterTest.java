package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

class CommandExecutorTest {
    Container pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
    GenericCommandDescription smallTreeCommand = new GenericCommandDescription("climb", "small", "tree");
    GenericCommandDescription matchingCommand = new GenericCommandDescription("climb", "small", "tree");
    GenericCommandDescription bigTreeCommand = new GenericCommandDescription("climb", "big", "tree");
    GenericCommandDescription partialCommand = new GenericCommandDescription("climb", "tree");
    ExecutionResult successResult = new ExecutionResult() {
        @Override
        public ExecutionResult.State getExecutionState() {
            return ExecutionResult.State.SUCCESS;
        }

        @Override
        public void setExecutionState(ExecutionResult.State anExecutionState) {
        }

        @Override
        public String getResultMessage() {
            return "You did it!";
        }

        @Override
        public void setResultMessage(String aResultMessage) {
        }

        @Override
        public boolean hasCommandMatched() {
            return true;
        }

        @Override
        public void setCommandHasMatched() {
        }
    };
    Action successAction = new Action() {
        @Override
        public ExecutionResult execute() {
            return successResult;
        }

        @Override
        public String getActionName() {
            return "SuccessAction";
        }
    };
    private final Container locationPocket = new GenericContainer(new DescriptionProvider("locationPocket"), 5);
    Location location = new Location(new DescriptionProvider("location"), locationPocket);
    CommandExecutor sut = new CommandExecutor(pocket, location);

    @Test
    void empptyPocketAndEmptyLocationCannotExecuteAnything() {
        // given
        assertThat(pocket.getCommands()).isEqualTo(List.of());
        assertThat(location.getCommands()).isEqualTo(List.of());

        // when
        ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);

        // when
        GenericCommandDescription noCommand = new GenericCommandDescription("");

        result = sut.execute(noCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
    }

    @Test
    void matchingPocketCommandMustExecuteSuccessfully() {
        // given
        pocket.addCommand(new GenericCommand(matchingCommand, successAction));

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingPocketCommandForItemMustExecuteSuccessfully() {
        // given
        Item item = new Item(new DescriptionProvider("tree"), true);
        item.addCommand(
                new GenericCommand(partialCommand, successAction)); // TODO: also test for complete command execution
        pocket.add(item);

        // when
        final ExecutionResult result = sut.execute(partialCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingLocationCommandMustExecuteSuccessfully() {
        // given
        location.addCommand(new GenericCommand(matchingCommand, successAction));

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingCommandForItemMustExecuteSuccessfully() {
        // given
        Item item = new Item(new DescriptionProvider("tree"), true);
        item.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(item);

        // when
        final ExecutionResult result = sut.execute(partialCommand); // TODO: also test for complete command execution

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void matchingCommandForAmbiguousItemsMustFail() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item anotherTree = new Item(new DescriptionProvider("tree"), true);
        anotherTree.addCommand(new GenericCommand(partialCommand, successAction));
        pocket.add(anotherTree);

        // when
        final ExecutionResult result = sut.execute(partialCommand); // TODO: also test for complete command execution

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo(
                String.format("What do you want to %s?", smallTreeCommand.getVerb()));
    }

    @Test
    void commandForSpecificItemMustExecuteSuccessfully() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item bigTree = new Item(new DescriptionProvider("big", "tree"), true);
        bigTree.addCommand(new GenericCommand(bigTreeCommand, successAction));
        location.getItemContainer().add(bigTree);
        Item smallTree = new Item(new DescriptionProvider("small", "tree"), true);
        smallTree.addCommand(new GenericCommand(matchingCommand, successAction));
        pocket.add(smallTree);

        // when
        final ExecutionResult result = sut.execute(smallTreeCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void ambiguousCommandForItemsWithDifferentNamesMustFail() {
        // given
        Item someTree = new Item(new DescriptionProvider("tree"), true);
        someTree.addCommand(new GenericCommand(partialCommand, successAction));
        location.getItemContainer().add(someTree);
        Item anotherTree = new Item(new DescriptionProvider("small", "tree"), true);
        anotherTree.addCommand(new GenericCommand(matchingCommand, successAction));
        pocket.add(anotherTree);

        // when
        final ExecutionResult result = sut.execute(partialCommand);

        // then
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("What do you want to climb?");
    }

}
