package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;

class CommandProviderDataTest {

    @Test
    void addingTwoCommandsWithTheSameSpecJoinsOneChainKeyedByStableId() {
        CommandProviderData provider = new CommandProviderData();
        Word verb = new Word("open", Word.Type.VERB);
        Word noun = new Word("door", Word.Type.NOUN);

        CommandData first = new CommandData(new CommandDescriptionData(verb, null, noun));
        first.setActions(List.of(new MessageActionData()));
        CommandData second = new CommandData(new CommandDescriptionData(verb, null, noun));
        second.setActions(List.of(new MessageActionData()));

        provider.add(first);
        provider.add(second);

        assertThat(provider.getAvailableCommands()).hasSize(1);
        String chainId = provider.getAvailableCommands().keySet().iterator().next();
        assertThat(chainId).isNotEqualTo("open||door");
        assertThat(provider.getAvailableCommands().get(chainId).getCommands())
                .containsExactly(first, second);
    }

    @Test
    void chainIdSurvivesEditingTheCommandsDescription() {
        CommandProviderData provider = new CommandProviderData();
        Word open = new Word("open", Word.Type.VERB);
        Word close = new Word("close", Word.Type.VERB);
        Word door = new Word("door", Word.Type.NOUN);

        CommandData command = new CommandData(new CommandDescriptionData(open, null, door));
        provider.add(command);
        String chainId = provider.getAvailableCommands().keySet().iterator().next();

        // author renames the trigger verb - the chain keeps its id, no map-key move needed
        command.setCommandDescription(new CommandDescriptionData(close, null, door));

        assertThat(provider.getAvailableCommands()).containsKey(chainId);
        assertThat(provider.getAvailableCommands().get(chainId).getCommands()).containsExactly(command);
    }

    @Test
    void findChainIdContaining_locatesTheChainHoldingAGivenCommand() {
        CommandProviderData provider = new CommandProviderData();
        Word verb = new Word("get", Word.Type.VERB);
        CommandData command = new CommandData(new CommandDescriptionData(verb, null, null));
        provider.add(command);
        String expectedChainId = provider.getAvailableCommands().keySet().iterator().next();

        Optional<String> found = provider.findChainIdContaining(command);

        assertThat(found).contains(expectedChainId);
    }

    @Test
    void findChainIdContaining_isEmptyForAnUnknownCommand() {
        CommandProviderData provider = new CommandProviderData();
        CommandData unknown = new CommandData();

        assertThat(provider.findChainIdContaining(unknown)).isEmpty();
    }
}
