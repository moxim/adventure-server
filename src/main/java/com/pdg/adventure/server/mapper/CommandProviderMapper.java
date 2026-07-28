package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 60, description = "Command provider mapping")
public class CommandProviderMapper implements Mapper<CommandProviderData, GenericCommandProvider> {

    private final Mapper<CommandChainData, CommandChain> commandChainMapper;
    private final MapperSupporter mapperSupporter;

    public CommandProviderMapper(MapperSupporter aMapperSupporter, CommandChainMapper aCommandChainMapper) {
        mapperSupporter = aMapperSupporter;
        commandChainMapper = aCommandChainMapper;
    }

    // mapToBO reads only chain VALUES, never the DO map's key - so a legacy document (keyed by
    // the old "verb|adjective|noun" spec string, from before commands were re-keyed by stable
    // chain id) loads correctly with no special-case handling, and mapToDO always re-keys by
    // the chain's own id on save. That's the whole migration: load, then save, once.
    @Override
    public GenericCommandProvider mapToBO(CommandProviderData aData) {
        GenericCommandProvider result = new GenericCommandProvider();
        result.setId(aData.getId());
        for (CommandChainData chainData : aData.getAvailableCommands().values()) {
            final CommandChain commandChain = commandChainMapper.mapToBO(chainData);
            final CommandDescription description = commandChain.getCommands().isEmpty()
                    ? new GenericCommandDescription(VocabularyData.EMPTY_STRING)
                    : commandChain.getCommands().getFirst().getDescription();
            result.getAvailableCommands().put(description, commandChain);
        }
        return result;
    }

    @Override
    public CommandProviderData mapToDO(GenericCommandProvider aData) {
        CommandProviderData result = new CommandProviderData();
        result.setId(aData.getId());
        for (CommandChain chain : aData.getAvailableCommands().values()) {
            final CommandChainData chainData = commandChainMapper.mapToDO(chain);
            result.getAvailableCommands().put(chainData.getId(), chainData);
        }
        return result;
    }

}
