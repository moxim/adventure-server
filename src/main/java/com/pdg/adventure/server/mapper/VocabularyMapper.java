package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {

    private MapperSupporter mapperSupporter;

    public VocabularyMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @Override
    public Vocabulary mapToBO(VocabularyData from) {
        Vocabulary vocabulary = mapperSupporter.getVocabulary();
        Collection<Word> words = from.getWords();
        vocabulary.setWords(words);
        return vocabulary;
    }

    @Override
    public VocabularyData mapToDO(Vocabulary from) {
        VocabularyData vocabularyData = new VocabularyData();
        vocabularyData.setWords(from.getWords());
        return vocabularyData;
    }
}
