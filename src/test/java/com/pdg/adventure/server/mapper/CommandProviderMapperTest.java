package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class CommandProviderMapperTest {

    @SuppressWarnings("unchecked")
    private CommandProviderMapper newSut(Vocabulary aVocabulary) {
        MapperSupporter mapperSupporter = mock(MapperSupporter.class);
        when(mapperSupporter.getVocabulary()).thenReturn(aVocabulary);

        Mapper<ActionData, Action> actionMapper = mock(Mapper.class);
        when(actionMapper.mapToBO(any())).thenReturn(new MessageAction("hi", new MessagesHolder()));
        when(mapperSupporter.getMapper(any())).thenReturn((Mapper) actionMapper);

        CommandDescriptionMapper descriptionMapper = new CommandDescriptionMapper(mapperSupporter);
        CommandMapper commandMapper = new CommandMapper(mapperSupporter, descriptionMapper);
        CommandChainMapper chainMapper = new CommandChainMapper(mapperSupporter, commandMapper);
        return new CommandProviderMapper(mapperSupporter, chainMapper);
    }

    @Test
    void mapToBO_ignoresTheLegacySpecStringKey_readsOnlyChainValues() {
        // given: a legacy document, as if written before this refactor - keyed by spec string
        Vocabulary vocabulary = new Vocabulary();
        Word take = vocabulary.createNewWord("take", Word.Type.VERB);
        Word sword = vocabulary.createNewWord("sword", Word.Type.NOUN);

        CommandProviderMapper sut = newSut(vocabulary);

        CommandData command = new CommandData(new CommandDescriptionData(take, null, sword));
        command.setActions(List.of(new MessageActionData()));
        CommandChainData chainData = new CommandChainData();
        chainData.getCommands().add(command);

        CommandProviderData legacy = new CommandProviderData();
        legacy.getAvailableCommands().put("take||sword", chainData); // legacy-shaped key

        // when
        GenericCommandProvider bo = sut.mapToBO(legacy);

        // then: exactly one chain, correctly readable regardless of the legacy key
        assertThat(bo.getAvailableCommands()).hasSize(1);
        CommandChain mappedChain = bo.getAvailableCommands().values().iterator().next();
        assertThat(mappedChain.getCommands()).hasSize(1);
    }

    @Test
    void roundTrip_reKeysToTheChainsOwnStableId_notADerivedString() {
        // Note: a fully-specified (verb+adjective+noun) description is used here rather than
        // an item-scoped empty-adjective one - CommandDescriptionMapper.mapToDO can't yet
        // round-trip an empty adjective/noun (vocabulary.findWord("") has nothing to find),
        // a pre-existing limitation orthogonal to this task's re-keying change.
        Vocabulary vocabulary = new Vocabulary();
        Word take = vocabulary.createNewWord("take", Word.Type.VERB);
        Word rusty = vocabulary.createNewWord("rusty", Word.Type.ADJECTIVE);
        Word sword = vocabulary.createNewWord("sword", Word.Type.NOUN);

        CommandProviderMapper sut = newSut(vocabulary);

        CommandData command = new CommandData(new CommandDescriptionData(take, rusty, sword));
        command.setActions(List.of(new MessageActionData()));
        CommandChainData chainData = new CommandChainData();
        chainData.getCommands().add(command);
        String originalChainId = chainData.getId();

        CommandProviderData legacy = new CommandProviderData();
        legacy.getAvailableCommands().put("take|rusty|sword", chainData);

        GenericCommandProvider bo = sut.mapToBO(legacy);
        CommandProviderData roundTripped = sut.mapToDO(bo);

        assertThat(roundTripped.getAvailableCommands()).containsKey(originalChainId);
        assertThat(roundTripped.getAvailableCommands()).doesNotContainKey("take|rusty|sword");
    }
}
