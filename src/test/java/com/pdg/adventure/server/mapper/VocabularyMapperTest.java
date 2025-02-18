package com.pdg.adventure.server.mapper;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class VocabularyMapperTest {

    @Autowired
    private MapperSupporter mapperSupporter;

    @Test
    void mapToDOandThenToBO() {
        Vocabulary vocabulary = mapperSupporter.getVocabulary();
        assertThat(vocabulary).isNotNull();
        vocabulary.createNewWord("word1", Word.Type.NOUN);
        final Mapper<VocabularyData, Vocabulary> sut = mapperSupporter.getMapper(Vocabulary.class);
        final VocabularyData vocabularyData = sut.mapToDO(vocabulary);
        assertThat(vocabularyData).isNotNull();
        assertThat(vocabularyData.findWord("word1")).isNotNull();

        final Vocabulary vocabulary1 = sut.mapToBO(vocabularyData);
        assertThat(vocabulary1).isNotNull();
        assertThat(vocabulary1.findSynonym("word1")).isNull();
        assertThat(vocabulary1.findWord("word1")).isNotNull();
    }
}
