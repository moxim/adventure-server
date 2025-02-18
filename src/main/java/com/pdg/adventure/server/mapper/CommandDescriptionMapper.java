package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
public class CommandDescriptionMapper implements Mapper<CommandDescriptionData, CommandDescription> {

    private final MapperSupporter mapperSupporter;

    public CommandDescriptionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
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
        CommandDescription result = new GenericCommandDescription(aCommandDescriptionData.getVerb().getText(),
                                                                  aCommandDescriptionData.getAdjective().getText(),
                                                                  aCommandDescriptionData.getNoun().getText());
        result.setId(aCommandDescriptionData.getId());
        return result;
    }
}
