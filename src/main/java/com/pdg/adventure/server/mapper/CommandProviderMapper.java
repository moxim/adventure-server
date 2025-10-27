package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.GenericCommandProvider;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 60, description = "Command provider mapping")
public class CommandProviderMapper implements Mapper<CommandProviderData, GenericCommandProvider> {

    private final Mapper<CommandChainData, CommandChain> commandChainMapper;
    private final Mapper<CommandDescriptionData, CommandDescription> commandDescriptionMapper;
    private final MapperSupporter mapperSupporter;

    public CommandProviderMapper(MapperSupporter aMapperSupporter,
                                 CommandChainMapper aCommandChainMapper,
                                 CommandDescriptionMapper aCommandDescriptionMapper) {
        mapperSupporter = aMapperSupporter;
        commandChainMapper = aCommandChainMapper;
        commandDescriptionMapper = aCommandDescriptionMapper;
        mapperSupporter.registerMapper(CommandProviderData.class, GenericCommandProvider.class, this);
    }

    @Override
    public GenericCommandProvider mapToBO(CommandProviderData aData) {
        GenericCommandProvider result = new GenericCommandProvider();
        result.setId(aData.getId());
        for (Map.Entry<String, CommandChainData> entry : aData.getAvailableCommands().entrySet()) {
            CommandDescriptionData commandDescriptionData = new CommandDescriptionData(entry.getKey());
            final CommandDescription description = commandDescriptionMapper.mapToBO(commandDescriptionData);
            final CommandChain commandChain = commandChainMapper.mapToBO(entry.getValue());
            result.getAvailableCommands().put(description, commandChain);
        }
        return result;
    }

    @Override
    public CommandProviderData mapToDO(GenericCommandProvider aData) {
        CommandProviderData result = new CommandProviderData();
        result.setId(aData.getId());
        for (Map.Entry<CommandDescription, CommandChain> entry : aData.getAvailableCommands().entrySet()) {
            final CommandDescriptionData descriptionData = commandDescriptionMapper.mapToDO(entry.getKey());
            final CommandChainData chainData = commandChainMapper.mapToDO(entry.getValue());
            result.getAvailableCommands().put(descriptionData.getCommandSpecification(), chainData);
        }
        return result;
    }

}
