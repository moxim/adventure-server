package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.stereotype.Service;

@Service
public class DescriptionMapper implements Mapper<DescriptionData, DescriptionProvider> {

    private MapperSupporter mapperSupporter;

    public DescriptionMapper(MapperSupporter aMapperSupporter) {
             mapperSupporter = aMapperSupporter;
         }

    @Override
    public DescriptionData mapToDO(DescriptionProvider aDescriptionProvider) {
        DescriptionData result = new DescriptionData();
        result.setId(aDescriptionProvider.getId());
        Vocabulary vocabulary = mapperSupporter.getVocabulary();
        result.setNoun(vocabulary.getSynonym(aDescriptionProvider.getNoun()));
        result.setAdjective(vocabulary.getSynonym(aDescriptionProvider.getAdjective()));
        result.setShortDescription(aDescriptionProvider.getShortDescription());
        result.setLongDescription(aDescriptionProvider.getLongDescription());
        return result;
    }

    @Override
    public DescriptionProvider mapToBO(DescriptionData aDescriptionData) {
        Word noun = aDescriptionData.getNoun();
        if (noun == null) {
            throw new RuntimeException(aDescriptionData.getLongDescription());
        }
        DescriptionProvider result = new DescriptionProvider(noun == null ? "" : noun.getText());
        result.setId(aDescriptionData.getId());
        result.setAdjective(aDescriptionData.getAdjective().getText());
        result.setShortDescription(aDescriptionData.getShortDescription());
        result.setLongDescription(aDescriptionData.getLongDescription());
        return result;
    }

}
