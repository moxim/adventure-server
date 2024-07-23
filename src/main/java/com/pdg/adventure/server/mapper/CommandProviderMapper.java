package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.parser.CommandProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommandProviderMapper implements Mapper<CommandProviderData, CommandProvider> {

    private final MapperSupporter mapperSupporter;

    public CommandProviderMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @Override
    public CommandProvider mapToBO(CommandProviderData aData) {
        CommandProvider result = new CommandProvider();
        result.setId(aData.getId());
        final CommandChainMapper commandChainMapper = mapperSupporter.getMapper(CommandChainMapper.class);
        final CommandDescriptionMapper commandDescriptionMapper = mapperSupporter.getMapper(CommandDescriptionMapper.class);
        for (Map.Entry<CommandDescriptionData, CommandChainData> entry : aData.getAvailableCommands().entrySet()) {
            final CommandDescription description = commandDescriptionMapper.mapToBO(entry.getKey());
            final CommandChain commandChain = commandChainMapper.mapToBO(entry.getValue());
            result.getAvailableCommands().put(description, commandChain);
        }
        return result;
    }

    @Override
    public CommandProviderData mapToDO(CommandProvider aData) {
        CommandProviderData result = new CommandProviderData();
        result.setId(aData.getId());
        final CommandChainMapper commandChainMapper = mapperSupporter.getMapper(CommandChainMapper.class);
        final CommandDescriptionMapper commandDescriptionMapper = mapperSupporter.getMapper(CommandDescriptionMapper.class);
        for (Map.Entry<CommandDescription, CommandChain> entry : aData.getAvailableCommands().entrySet()) {
            final CommandDescriptionData descriptionData = commandDescriptionMapper.mapToDO(entry.getKey());
            final CommandChainData chainData = commandChainMapper.mapToDO(entry.getValue());
            result.getAvailableCommands().put(descriptionData, chainData);
        }
        return result;
    }

}
