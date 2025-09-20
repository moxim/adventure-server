package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
@AutoRegisterMapper(priority = 15, description = "Core description mapping")
public class DescriptionMapper implements Mapper<DescriptionData, DescriptionProvider> {

    private final MapperSupporter mapperSupporter;
    private Vocabulary vocabulary;

    public DescriptionMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void initializeDependencies() {
        vocabulary = mapperSupporter.getVocabulary();
    }

    @Override
    public DescriptionData mapToDO(DescriptionProvider aDescriptionProvider) {
        DescriptionData result = new DescriptionData();
        result.setId(aDescriptionProvider.getId());
        result.setNoun(vocabulary.findSynonym(aDescriptionProvider.getNoun()));
        result.setAdjective(vocabulary.findSynonym(aDescriptionProvider.getAdjective()));
        result.setShortDescription(aDescriptionProvider.getShortDescription());
        result.setLongDescription(aDescriptionProvider.getLongDescription());
        return result;
    }

    @Override
    public DescriptionProvider mapToBO(DescriptionData aDescriptionData) {
        Word noun = aDescriptionData.getNoun();
        DescriptionProvider result = new DescriptionProvider(noun == null ? "" : noun.getText());
        result.setId(aDescriptionData.getId());
        result.setAdjective(aDescriptionData.getAdjective().getText());
        result.setShortDescription(aDescriptionData.getShortDescription());
        result.setLongDescription(aDescriptionData.getLongDescription());
        return result;
    }

}
