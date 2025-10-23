package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class AutoMapperRegistrationTest {

    @Autowired
    private MapperSupporter mapperSupporter;

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @Autowired
    private DescriptionMapper descriptionMapper;

    @Test
    void testVocabularyMapperRegistration() {
        Mapper<VocabularyData, Vocabulary> mapper = vocabularyMapper;

        assertNotNull(mapper, "VocabularyMapper should be auto-registered");
    }

    @Test
    void testDescriptionMapperRegistration() {
        Mapper<DescriptionData, DescriptionProvider> mapper = descriptionMapper;

        assertNotNull(mapper, "DescriptionMapper should be auto-registered");
    }

    @Test
    void testMapperSupporterInitialization() {
        assertNotNull(mapperSupporter, "MapperSupporter should be properly injected");
        assertNotNull(mapperSupporter.getVocabulary(), "Vocabulary should be available");
    }
}
