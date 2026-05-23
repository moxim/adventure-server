package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@ExtendWith(MockitoExtension.class)
class VocabularyMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    private VocabularyMapper vocabularyMapper;

    @BeforeEach
    void setUp() {
        when(mapperSupporter.getVocabulary()).thenReturn(new Vocabulary());
        vocabularyMapper = new VocabularyMapper(mapperSupporter);
    }

    @Test
    void mapToDOandThenToBO() {
        // given
        Vocabulary vocabulary = mapperSupporter.getVocabulary();

        // then
        assertThat(vocabulary).isNotNull();

        // when
        vocabulary.createNewWord("word1", Word.Type.NOUN);
        final Mapper<VocabularyData, Vocabulary> sut = vocabularyMapper;
        final VocabularyData vocabularyData = sut.mapToDO(vocabulary);

        // then
        assertThat(vocabularyData).isNotNull();
        assertThat(vocabularyData.findWord("word1")).isNotNull();

        // when
        when(mapperSupporter.getVocabulary()).thenReturn(new Vocabulary());
        final Vocabulary vocabulary1 = sut.mapToBO(vocabularyData);

        // then
        assertThat(vocabulary1).isNotNull();
        assertThat(vocabulary1.findSynonym("word1")).isNull();
        assertThat(vocabulary1.findWord("word1")).isNotNull();
    }
}
