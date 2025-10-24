package com.pdg.adventure.server.mapper;

import org.springframework.stereotype.Service;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
@AutoRegisterMapper(priority = 20, description = "Command description mapping with vocabulary")
public class CommandDescriptionMapper implements Mapper<CommandDescriptionData, CommandDescription> {

    private final MapperSupporter mapperSupporter;

    public CommandDescriptionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
        mapperSupporter.registerMapper(CommandDescriptionData.class, CommandDescription.class, this);
    }

    @Override
    public CommandDescriptionData mapToDO(CommandDescription aCommandDescription) {
        CommandDescriptionData result = new CommandDescriptionData();
        final Vocabulary vocabulary = mapperSupporter.getVocabulary();
        result.setId(aCommandDescription.getId());
        result.setVerb(vocabulary.findWord(aCommandDescription.getVerb()).orElseThrow());
        result.setAdjective(vocabulary.findWord(aCommandDescription.getAdjective()).orElseThrow());
        result.setNoun(vocabulary.findWord(aCommandDescription.getNoun()).orElseThrow());
        return result;
    }

    @Override
    public CommandDescription mapToBO(CommandDescriptionData aCommandDescriptionData) {
        var verb = aCommandDescriptionData.getVerb() == null ? "" : aCommandDescriptionData.getVerb().getText();
        var adjective = aCommandDescriptionData.getAdjective() == null ? "" : aCommandDescriptionData.getAdjective().getText();
        var noun = aCommandDescriptionData.getNoun() == null ? "" : aCommandDescriptionData.getNoun().getText();
        CommandDescription result = new GenericCommandDescription(verb, adjective, noun);
        result.setId(aCommandDescriptionData.getId());
        return result;
    }
}
