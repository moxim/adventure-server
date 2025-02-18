package com.pdg.adventure.server.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Collection;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@Service
public class VocabularyMapper implements Mapper<VocabularyData, Vocabulary> {

    private final MapperSupporter mapperSupporter;

    public VocabularyMapper(MapperSupporter aMapperSupporter) {
        mapperSupporter = aMapperSupporter;
    }

    @PostConstruct
    public void registerMapper() {
        mapperSupporter.registerMapper(VocabularyData.class, Vocabulary.class, this);
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
